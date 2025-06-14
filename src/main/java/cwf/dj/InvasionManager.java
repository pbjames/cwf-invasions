package cwf.dj;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class InvasionManager {
  public static final int FAST_CHECK_TIME = 5 * 20;
  public static final int SLOW_CHECK_TIME = 10 * 20;
  public static final int MAX_INVADE_DIST = 2 * 16;
  public static final int MIN_INVADE_DIST = 5; // personal space
  public static final Logger LOGGER = CWFInvasions.logger;
  public static boolean invasionHappeningNow;
  public static boolean graceIsNow;
  public static final Random RANDOM = new Random();

  @SubscribeEvent
  public static void onTickServerSlow(ServerTickEvent event) {
    if (event.phase == Phase.START) {
      World world = DimensionManager.getWorld(0);
      if (world == null) return;
      if (world.getTotalWorldTime() % SLOW_CHECK_TIME != 0) return;
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
        checkSetInvasion(player);
      }
    }
  }

  public static void checkSetInvasion(@Nonnull EntityPlayerMP player) {
    InventoryPlayer playerInv = player.inventory;
    if (playerInv == null) return;
    int currentItem = player.inventory.currentItem;
    Item heldItem = playerInv.mainInventory.get(currentItem).getItem();
    if (heldItem == Items.CHICKEN) {
      invasionHappeningNow = true;
      LOGGER.info("Invasion starting");
    } else if (heldItem == Items.COOKED_CHICKEN) {
      LOGGER.info("Invasion ending");
      invasionHappeningNow = false;
    }
    LOGGER.info("Held item during check: ", heldItem.toString());
  }

  @SubscribeEvent
  public static void onTickServerFast(ServerTickEvent event) {
    if (event.phase == Phase.START) {
      if (!invasionHappeningNow) return;
      World world = DimensionManager.getWorld(0);
      if (world == null) return;
      if (world.getTotalWorldTime() % FAST_CHECK_TIME != 0) return;
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
        invade(player);
      }
    }
  }

  public static void invade(EntityPlayerMP player) {
    BlockPos spawnPos = findAirBlockNear(player);
    World world = player.world;
    EntityZombie zombie = new EntityZombie(world);
    //zombie.tasks.addTask(2, new EntityAIOmniSetTarget<EntityPlayerMP>(zombie, player));
    //zombie.tasks.addTask(2, new EntityAIOmniAttackMelee(zombie, 4.0D));
    zombie.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
    zombie.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    world.spawnEntity(zombie);
  }

  public static BlockPos findAirBlockNear(EntityPlayer player) {
    // TODO: Worry about optimisation later
    LOGGER.info("Searching space for spawn");
    int maxDistSq = MAX_INVADE_DIST * MAX_INVADE_DIST;
    int minDistSq = MIN_INVADE_DIST * MIN_INVADE_DIST;
    World world = player.world;
    BlockPos start = player.getPosition();
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(start);
    visited.add(start);
    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      double distFromStart = start.distanceSq(pos);
      LOGGER.info(
          "Distance from start: {}, min dist sq: {}, max dist sq: {}",
          distFromStart,
          minDistSq,
          maxDistSq);
      IBlockState state = world.getBlockState(pos);
      if (state.getBlock() == Blocks.AIR
          && (distFromStart > minDistSq)
          && (distFromStart < maxDistSq)) return pos;
      for (BlockPos offset : getNeighborOffsets()) {
        BlockPos neighbor = pos.add(offset);
        LOGGER.info("Try neighbour: {}", neighbor);
        if (!visited.contains(neighbor)) {
          LOGGER.info("Add neighbour: {}", neighbor);
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }
    }
    LOGGER.info("Not satisfied");
    return player.getPosition();
  }

  private static List<BlockPos> getNeighborOffsets() {
    // drunk walk
    int xOffs = RANDOM.nextInt(4) - 2;
    int zOffs = RANDOM.nextInt(4) - 2;
    return Arrays.asList(
        new BlockPos(0, -1, 0),
        new BlockPos(xOffs, 0, 0),
        new BlockPos(0, 0, zOffs),
        new BlockPos(1, 0, 0),
        new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1),
        new BlockPos(0, 0, -1),
        new BlockPos(0, 1, 0));
  }

  // @SubscribeEvent
  // public void onEntityTick(LivingUpdateEvent event) { }
}
