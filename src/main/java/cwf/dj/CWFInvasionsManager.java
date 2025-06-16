package cwf.dj;

import cwf.dj.invasion_config.InvadeMobClass;
import cwf.dj.invasion_config.InvasionConfig;
import cwf.dj.invasion_config.InvasionConfigCollection;
import cwf.dj.tasks.EntityAIChaseMelee;
import cwf.dj.tasks.EntityAIOmniSetTarget;
import java.lang.reflect.InvocationTargetException;
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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class CWFInvasionsManager {
  private static boolean invasionHappeningNow = false;
  private static long timeAtStart;
  private static int slainSinceStart;
  public static boolean graceIsNow;
  public static List<Entity> activeMobs = new ArrayList<>();
  public static final Logger LOGGER = CWFInvasions.logger;
  public static final Random RANDOM = new Random();
  public static final TextComponentString INVASION_STARTED_MSG =
      new TextComponentString("§l§cINVASION STARTED§r");
  public static final TextComponentString INVASION_ENDED_MSG =
      new TextComponentString("§l§6INVASION ENDED§r");
  // INFO: For multiple invasions we pick the first match for start conditions
  // and stick with that for the end condition
  // Perhaps in the future, we could manager multiple invasions simultaneously
  public static InvasionConfig chosenConfig;

  @SubscribeEvent
  public static void onTickServer(ServerTickEvent event) {
    if (event.phase != Phase.START) return;
    World world = DimensionManager.getWorld(0);
    if (world == null) return;
    int slowCheckTime = Configuration.common.slowTickTime;
    int fastCheckTime = Configuration.common.fastTickTime;
    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
    if (world.getTotalWorldTime() % fastCheckTime == 0)
      for (EntityPlayerMP player : players) if (getInvasion()) invade(player, players.size());
    if (world.getTotalWorldTime() % slowCheckTime == 0)
      players.forEach(player -> checkSetInvasion(player));
  }

  public static void spawnFromConfig(EntityPlayerMP player) {
    BlockPos spawnPos = findAirBlockNear(player);
    World world = player.world;
    InvadeMobClass mobClass = chosenConfig.pickRandomMobClass();
    EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(mobClass.ent));
    try {
      EntityCreature zombie =
          (EntityCreature) entry.getEntityClass().getConstructor(World.class).newInstance(world);
      zombie.tasks.addTask(2, new EntityAIOmniSetTarget<EntityPlayerMP>(zombie, player));
      zombie.tasks.addTask(2, new EntityAIChaseMelee<EntityPlayerMP>(zombie, 1.0D, player));
      zombie.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
      zombie.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
      world.spawnEntity(zombie);
      activeMobs.add(zombie);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
    }
  }

  public static void removeDeadMobs() {
    int oldCount = activeMobs.size();
    activeMobs = activeMobs.stream().filter(mob -> !mob.isDead).collect(Collectors.toList());
    slainSinceStart += oldCount - activeMobs.size();
  }

  public static void checkSetInvasion(@Nonnull EntityPlayerMP player) {
    if (getInvasion()) {
      checkForEnding(chosenConfig);
    } else {
      for (InvasionConfig config : InvasionConfigCollection.configs) {
        if (checkForStarting(config)) {
          chosenConfig = config;
          return;
        }
      }
    }
  }

  private static boolean checkForStarting(InvasionConfig config) {
    World world = DimensionManager.getWorld(0);
    LOGGER.info("Want to start invasion, daytime: {}", world.isDaytime());
    switch (config.startCondition) {
      case FORTNIGHT:
        if (world.getTotalWorldTime() % (14 * 24000) == 0) {
          startInvasion();
          return true;
        }
        break;
      case FULL_MOON:
        if (world.getMoonPhase() == 0) {
          startInvasion();
          return true;
        }
        break;
      case NIGHT:
        if (!world.isDaytime()) {
          startInvasion();
          return true;
        }
        break;
      case DAY:
        if (world.isDaytime()) {
          startInvasion();
          return true;
        }
        break;
    }
    return false;
  }

  private static boolean checkForEnding(InvasionConfig config) {
    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
    switch (config.endingCondition) {
      case MOBCOUNT:
        if (slainSinceStart * players.size() >= config.mobCountToEnd) {
          endInvasion();
          return true;
        }
        break;
      case TIME:
        long timeDelta = DimensionManager.getWorld(0).getTotalWorldTime() - timeAtStart;
        if (timeDelta >= config.timeToEndTicks) {
          endInvasion();
          return true;
        }
        break;
    }
    return false;
  }

  public static void invade(EntityPlayerMP player, int playerCount) {
    int maintenanceLevel = Configuration.common.mobMaintainCount;
    removeDeadMobs();
    if (activeMobs.size() >= (maintenanceLevel * playerCount)) return;
    spawnFromConfig(player);
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

  public static void startInvasion() {
    LOGGER.info("Invasion starting");
    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
    players.forEach(player -> player.sendMessage(INVASION_STARTED_MSG));
    invasionHappeningNow = true;
    slainSinceStart = 0;
    timeAtStart = DimensionManager.getWorld(0).getTotalWorldTime();
  }

  public static void endInvasion() {
    LOGGER.info("Invasion ending");
    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
    players.forEach(player -> player.sendMessage(INVASION_ENDED_MSG));
    invasionHappeningNow = false;
  }

  public static boolean getInvasion() {
    return invasionHappeningNow;
  }

  // @SubscribeEvent
  // public void onEntityTick(LivingUpdateEvent event) { }
}
