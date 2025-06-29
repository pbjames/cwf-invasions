package cwf.dj.tasks;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

public class EntityAIMineToTarget<T extends EntityLivingBase> extends EntityAIBase {
  class DijkstraNode {
    public double totalWeight;
    public BlockPos pos;
    public @Nullable DijkstraNode parent;

    DijkstraNode(double totalWeight, BlockPos pos, @Nullable DijkstraNode parent) {
      this.totalWeight = totalWeight;
      this.pos = pos;
      this.parent = parent;
    }

    public String toString() {
      return String.format(
          "DijkstraNode<totalWeight=%.2f,weight=%.2f@x=%d,y=%d,z=%d>",
          totalWeight, getWeight(), pos.getX(), pos.getY(), pos.getZ());
    }

    double getWeight() {
      double sqDistanceToEntity =
          targetEntity.getPosition().distanceSq(pos.getX(), pos.getY(), pos.getZ());
      return getWeightOfPos(pos.down(), Double.POSITIVE_INFINITY)
          + getWeightOfPos(pos, 0)
          + getWeightOfPos(pos.up(), 0)
          + sqDistanceToEntity / 10;
    }

    double getWeightOfPos(BlockPos posIn, double airValue) {
      IBlockState state = world.getBlockState(posIn);
      double hardness = state.getBlockHardness(world, posIn);
      boolean indestructible = hardness < 0D;
      boolean isLavaAdjacent =
          getNeighbours().stream()
              .anyMatch(p -> world.getBlockState(p).getBlock().equals(Blocks.LAVA));
      boolean isAir = world.getBlockState(posIn).getBlock().equals(Blocks.AIR);
      return hardness
          + (isAir ? airValue : 0)
          + (indestructible && !isAir || isLavaAdjacent ? Double.POSITIVE_INFINITY : 0D);
    }

    List<BlockPos> getNeighbours() {
      return Arrays.asList(
          new BlockPos[] {pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west()});
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.totalWeight, this.pos);
    }
  }

  private static final Logger LOGGER = cwf.dj.CWFInvasions.logger;
  private final World world;
  private final EntityCreature us;
  private final T targetEntity;
  private @Nullable Vec3d lastTrackedPosition;
  private int idleTicks = 0;
  private final int IDLE_THRESHOLD = 20;
  private Queue<BlockPos> path = new ArrayDeque<>();
  private int miningTick = 0;

  public EntityAIMineToTarget(EntityCreature creature, double speedIn, T target) {
    this.us = creature;
    this.world = creature.world;
    this.targetEntity = target;
    this.setMutexBits(3);
  }

  /** Returns whether the EntityAIBase should begin execution. */
  @Override
  public boolean shouldExecute() {
    LOGGER.info("Should execute: idle {}, onGround {}", isIdle(), us.onGround);
    return isIdle() && us.onGround;
  }

  /** Returns whether an in-progress EntityAIBase should continue executing */
  @Override
  public boolean shouldContinueExecuting() {
    // INFO: Could be ineffecient
    LOGGER.info(
        "Should continue executing?: path {}, onGround {}",
        us.getNavigator().getPathToEntityLiving(targetEntity),
        us.onGround);
    return us.getNavigator().getPathToEntityLiving(targetEntity) == null && us.onGround;
  }

  /** Execute a one shot task or start executing a continuous task */
  @Override
  public void startExecuting() {
    us.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
    setMiningPathToPos(targetEntity.getPosition());
    LOGGER.info("Mining AI started executing");
  }

  /** Reset the task's internal state. Called when this task is interrupted by another one */
  @Override
  public void resetTask() {
    us.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.AIR));
    LOGGER.info("Mining AI was interrupted");
  }

  /** Keep ticking a continuous task that has already been started */
  @Override
  public void updateTask() {
    // mine 2 * 3 blocks, then get path to end, repeat
    LOGGER.info("Path length: {}", path.size());
    while (!path.isEmpty()) {
      miningTick++;
      BlockPos miningPos = path.poll();
      LOGGER.info("CONSUMING PATH");
      // world.setBlockState(miningPos, Blocks.DIAMOND_BLOCK.getDefaultState());
      world.destroyBlock(miningPos, true);
      world.destroyBlock(miningPos.add(0, 1, 0), true);
      if (miningTick % 3 == 0)
        us.getNavigator().tryMoveToXYZ(miningPos.getX(), miningPos.getY(), miningPos.getZ(), 1);
    }
  }

  @Nullable
  private void setMiningPathToPos(BlockPos pos) {
    Set<BlockPos> visited = new HashSet<>();
    PriorityQueue<DijkstraNode> minHeap =
        new PriorityQueue<>(Comparator.comparingDouble(dn -> dn.totalWeight));
    minHeap.add(new DijkstraNode(0D, us.getPosition(), null));
    boolean goingUp = false;
    while (!minHeap.isEmpty()) {
      DijkstraNode root = minHeap.poll();
      visited.add(root.pos);
      LOGGER.info("Visited Node {} with block {}", root, world.getBlockState(root.pos).getBlock());
      if (root.pos.equals(pos)) {
        while (root != null) {
          path.add(root.pos);
          root = root.parent;
        }
        return;
      }
      for (BlockPos neighbour : root.getNeighbours()) {
        if (visited.contains(neighbour)) continue;
        if (neighbour.equals(root.pos.up())) {
          if (goingUp) {
            goingUp = false;
            continue;
          } else goingUp = true;
        }
        minHeap.add(new DijkstraNode(root.totalWeight + root.getWeight(), neighbour, root));
        if (root.totalWeight == Double.POSITIVE_INFINITY) return;
      }
    }
  }

  private boolean isIdle() {
    if (lastTrackedPosition == null) {
      lastTrackedPosition = us.getPositionVector();
      return false;
    }
    Vec3d currentPosition = us.getPositionVector();
    double sqDistTravelled = currentPosition.squareDistanceTo(lastTrackedPosition);
    lastTrackedPosition = currentPosition;
    if (sqDistTravelled < 0.1D) idleTicks++;
    else idleTicks = 0;
    return idleTicks > IDLE_THRESHOLD;
  }
}
