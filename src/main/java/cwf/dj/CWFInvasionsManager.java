package cwf.dj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
public class CWFInvasionsManager {
  public static boolean invasionHappeningNow;
  public static boolean graceIsNow;
  public static List<Entity> activeMobs = new ArrayList<>();
  public static final Logger LOGGER = CWFInvasions.logger;
  public static final Random RANDOM = new Random();

  @SubscribeEvent
  public static void onTickServerSlow(ServerTickEvent event) {
    int slowCheckTime = Configuration.common.slowTickTime;
    if (event.phase == Phase.START) {
      World world = DimensionManager.getWorld(0);
      if (world == null) return;
      if (world.getTotalWorldTime() % slowCheckTime != 0) return;
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
      players.forEach(player -> checkSetInvasion(player));
    }
  }

  @SubscribeEvent
  public static void onTickServerFast(ServerTickEvent event) {
    int fastCheckTime = Configuration.common.fastTickTime;
    if (event.phase == Phase.START) {
      if (!invasionHappeningNow) return;
      World world = DimensionManager.getWorld(0);
      if (world == null) return;
      if (world.getTotalWorldTime() % fastCheckTime != 0) return;
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
      players.forEach(player -> invade(player, players.size()));
    }
  }

  public static void removeDeadMobs() {
    activeMobs = activeMobs.stream().filter(mob -> !mob.isDead).collect(Collectors.toList());
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

  public static void invade(EntityPlayerMP player, int playerCount) {
    BlockPos spawnPos = findAirBlockNear(player);
    World world = player.world;
    int maintenanceLevel = Configuration.common.mobMaintainCount;
    removeDeadMobs();
    if (activeMobs.size() >= maintenanceLevel * playerCount) return;
    EntityZombie zombie = new EntityZombie(world);
    // zombie.tasks.addTask(2, new EntityAIOmniSetTarget<EntityPlayerMP>(zombie, player));
    // zombie.tasks.addTask(2, new EntityAIOmniAttackMelee(zombie, 4.0D));
    zombie.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
    zombie.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    world.spawnEntity(zombie);
    activeMobs.add(zombie);
  }

  public static BlockPos findAirBlockNear(EntityPlayer player) {
    // TODO: Worry about optimisation later
    LOGGER.info("Searching space for spawn");
    int maxDistSq = Configuration.common.maxInvadeDistance * Configuration.common.maxInvadeDistance;
    int minDistSq = Configuration.common.minInvadeDistance * Configuration.common.minInvadeDistance;
    World world = player.world;
    BlockPos start = player.getPosition();
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(start);
    visited.add(start);
    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      double distFromStart = start.distanceSq(pos);
      IBlockState state = world.getBlockState(pos);
      if (state.getBlock() == Blocks.AIR
          && (distFromStart > minDistSq)
          && (distFromStart < maxDistSq)) return pos;
      for (BlockPos offset : getNeighborOffsets()) {
        BlockPos neighbor = pos.add(offset);
        if (!visited.contains(neighbor)) {
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }
    }
    LOGGER.info("Position not satisfied, being aggressive");
    return player.getPosition();
  }

  private static List<BlockPos> getNeighborOffsets() {
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
