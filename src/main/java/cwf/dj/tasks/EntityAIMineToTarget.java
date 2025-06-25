package cwf.dj.tasks;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIMineToTarget<T extends EntityLivingBase> extends EntityAIBase {
  private static final Logger LOGGER = cwf.dj.CWFInvasions.logger;
  private final World world;
  private final EntityCreature us;
  private final T targetEntity;
  private @Nullable Vec3d lastTrackedPosition;
  private int idleTicks = 0;
  private final int IDLE_THRESHOLD = 5;
  private NodeProcessor nodeProcessor;

  public EntityAIMineToTarget(EntityCreature creature, double speedIn, T target) {
    this.us = creature;
    this.world = creature.world;
    this.targetEntity = target;
    this.setMutexBits(3);
    this.nodeProcessor = new WalkNodeProcessor();
    this.nodeProcessor.setCanEnterDoors(true);
  }

  /** Returns whether the EntityAIBase should begin execution. */
  @Override
  public boolean shouldExecute() {
    return isIdle() && us.onGround;
  }

  /** Returns whether an in-progress EntityAIBase should continue executing */
  @Override
  public boolean shouldContinueExecuting() {
    //PathNodeType pathNodeType = nodeProcessor.getPathNodeType(world, x, y, z, entitylivingIn, xSize, ySize, zSize, canBreakDoorsIn, canEnterDoorsIn)
    return false;
  }

  /** Execute a one shot task or start executing a continuous task */
  @Override
  public void startExecuting() {
  }

  /** Reset the task's internal state. Called when this task is interrupted by another one */
  @Override
  public void resetTask() {
  }

  /** Keep ticking a continuous task that has already been started */
  @Override
  public void updateTask() {
  }

  private boolean isIdle() {
    if (lastTrackedPosition == null) {
      lastTrackedPosition = us.getPositionVector();
      return false;
    }
    double sqDistTravelled = us.getPositionVector().squareDistanceTo(lastTrackedPosition);
    if (sqDistTravelled < 0.01D)
      idleTicks++;
    else idleTicks = 0;
    return idleTicks > IDLE_THRESHOLD;
  }
}
