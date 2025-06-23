package cwf.dj;

import cwf.dj.invasion_config.InvadeMobClass;
import cwf.dj.invasion_config.InvasionConfig;
import cwf.dj.invasion_config.InvasionConfigCollection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class CWFInvasionsManager {
  private static ManagerDataStore data;
  public static Set<Entity> activeMobs = new HashSet<>();
  public static final Random RANDOM = new Random();
  public static final Logger LOGGER = CWFInvasions.logger;
  public static final MinecraftServer SERVER =
      FMLCommonHandler.instance().getMinecraftServerInstance();
  public static final TextComponentString INVASION_STARTED_MSG =
      new TextComponentString("§l§cINVASION STARTED§r");
  public static final TextComponentString INVASION_ENDED_MSG =
      new TextComponentString("§l§6INVASION ENDED§r");
  // INFO: Valid on the assumption this code runs on server ticks, and usually when
  // there's a minecraft server there's also a non-null world to go along with it.
  public static World world = DimensionManager.getWorld(0);

  @SubscribeEvent
  public static void onTickServer(ServerTickEvent event) {
    if (event.phase != Phase.START) return;
    if (world == null) return;
    if (data == null) return;
    int slowCheckTime = Configuration.common.slowTickTime;
    int fastCheckTime = Configuration.common.fastTickTime;
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    if (world.getTotalWorldTime() % fastCheckTime == 0)
      for (EntityPlayerMP player : players) if (getInvasion()) invade(player, players.size());
    if (world.getTotalWorldTime() % slowCheckTime == 0) {
      players.forEach(player -> checkSetInvasion(player));
    }
  }

  @SubscribeEvent
  public static void onMobDeath(LivingDeathEvent event) {
    EntityLivingBase entity = event.getEntityLiving();
    if (!activeMobs.contains(entity)) return;
    DamageSource source = event.getSource();
    if (source.getTrueSource() != null) {
      if (source.getTrueSource() instanceof EntityPlayer) {
        activeMobs.remove(entity);
        data.slainSinceStart += 1;
        data.markDirty();
      }
    }
  }

  public static void createAndStoreData(World worldIn) {
    // INFO: Assume if we have no world data, there's no invasion history
    ManagerDataStore data = new ManagerDataStore();
    worldIn.getMapStorage().setData(ManagerDataStore.NAME, data);
    data.markDirty();
    setData(data);
    setWorld(worldIn);
  }

  public static void loadFromWorld(World worldIn) {
    ManagerDataStore dataFrom = ManagerDataStore.retrieveData(worldIn);
    setData(dataFrom);
    setWorld(worldIn);
  }

  public static void logConfig() {
    LOGGER.info(
        "logConfig():"
            + " CWFInvasionsManager[invasion={},timeAtStart={},slainSinceStart={},configName={}]",
        data.invasionHappeningNow,
        data.timeAtStart,
        data.slainSinceStart,
        data.configName);
  }

  public static void startInvasion(InvasionConfig config) {
    LOGGER.info("Invasion starting");
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    players.forEach(player -> player.sendMessage(INVASION_STARTED_MSG));
    config.__cooldownTSDontChangeMePlz = world.getTotalWorldTime();
    data.invasionHappeningNow = true;
    data.slainSinceStart = 0;
    data.timeAtStart = world.getTotalWorldTime();
    data.markDirty();
  }

  public static void endInvasion() {
    LOGGER.info("Invasion ending");
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    players.forEach(player -> player.sendMessage(INVASION_ENDED_MSG));
    data.invasionHappeningNow = false;
    data.markDirty();
  }

  public static boolean getInvasion() {
    return data.invasionHappeningNow;
  }

  private static void invade(EntityPlayerMP player, int playerCount) {
    int maintenanceLevel = getChosenConfig().maintainedPopulation;
    if (activeMobs.size() >= (maintenanceLevel * playerCount)) return;
    spawnFromConfig(player);
  }

  @Nullable
  private static InvasionConfig getChosenConfig() {
    return InvasionConfigCollection.configs.get(data.configName);
  }

  private static BlockPos findAirBlockNear(EntityPlayer player, int minDist, int maxDist) {
    int maxDistSq = maxDist * maxDist;
    int minDistSq = minDist * minDist;
    int distDiff = maxDist - minDist;
    int randomX = RANDOM.nextInt(2 * distDiff) - distDiff + minDist;
    int randomZ = RANDOM.nextInt(2 * distDiff) - distDiff + minDist;
    BlockPos start = player.getPosition();
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(new BlockPos(start.add(randomX, player.getEyeHeight(), randomZ)));
    visited.add(start);
    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      BlockPos adjustedPos = castDownBlockPos(player.world, pos);
      IBlockState state = player.world.getBlockState(pos);
      double distFromStart = start.distanceSq(adjustedPos);
      if (state.getBlock() == Blocks.AIR
          && (distFromStart > minDistSq)
          && (distFromStart < maxDistSq)) return adjustedPos.add(0, player.getEyeHeight(), 0);
      for (BlockPos offset : getNeighborOffsets()) {
        BlockPos neighbor = pos.add(offset);
        if (!visited.contains(neighbor)) {
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }
    }
    LOGGER.info("Position not satisfied for spawning, being aggressive");
    return player.getPosition();
  }

  private static void spawnFromConfig(EntityPlayerMP player) {
    InvadeMobClass mobClass = getChosenConfig().pickRandomMobClass();
    double healthScale = getChosenConfig().getHealthFactor();
    double damageScale = getChosenConfig().getDamageFactor();
    BlockPos spawnPos = findAirBlockNear(player, mobClass.minDist, mobClass.maxDist);
    Entity spawnedEntity = mobClass.spawn(player, spawnPos, healthScale, damageScale);
    activeMobs.add(spawnedEntity);
  }

  private static void checkSetInvasion(@Nonnull EntityPlayerMP player) {
    if (getInvasion()) {
      checkForEnding(getChosenConfig());
    } else {
      // INFO: For multiple invasions we pick the first match for start conditions
      // and stick with that for the end condition
      // Perhaps in the future, we could manager multiple invasions simultaneously
      for (InvasionConfig config : InvasionConfigCollection.configs.values()) {
        if (checkForStarting(config)) {
          data.configName = reverseMap(InvasionConfigCollection.configs).get(config);
          data.markDirty();
          return;
        }
      }
    }
  }

  private static void setWorld(World worldIn) {
    world = worldIn;
  }

  private static void setData(ManagerDataStore dataIn) {
    data = dataIn;
  }

  private static <K, V> Map<V, K> reverseMap(Map<K, V> original) {
    Map<V, K> reversed = new HashMap<>();
    for (Map.Entry<K, V> entry : original.entrySet())
      reversed.put(entry.getValue(), entry.getKey());
    return reversed;
  }

  private static boolean checkForStarting(InvasionConfig config) {
    long currentTime = world.getTotalWorldTime();
    if (config.__cooldownTSDontChangeMePlz == 0) config.__cooldownTSDontChangeMePlz = currentTime;
    LOGGER.info("Checking for invasion start");
    long ticksSinceCooldown = currentTime - config.__cooldownTSDontChangeMePlz;
    if (ticksSinceCooldown < config.cooldownTicks) {
      LOGGER.info(
          "Cooldown active, not invading: {} - {} < {}",
          world.getTotalWorldTime(),
          config.__cooldownTSDontChangeMePlz,
          config.cooldownTicks);
      return false;
    }
    switch (config.startCondition) {
      case FORTNIGHT:
        if (world.getTotalWorldTime() % (14 * 24000) == 0) {
          startInvasion(config);
          return true;
        }
        break;
      case FULL_MOON:
        if (world.getMoonPhase() == 0) {
          startInvasion(config);
          return true;
        }
        break;
      case NIGHT:
        long time = world.getWorldTime();
        if (23000 > time && time > 13000) {
          startInvasion(config);
          return true;
        }
        break;
      case DAY:
        if (world.getWorldTime() == 0) {
          startInvasion(config);
          return true;
        }
        break;
    }
    return false;
  }

  private static boolean checkForEnding(InvasionConfig config) {
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    switch (config.endingCondition) {
      case MOBCOUNT:
        if (data.slainSinceStart / players.size() >= config.mobCountToEnd) {
          endInvasion();
          return true;
        }
        break;
      case TIME:
        long timeDelta = world.getTotalWorldTime() - data.timeAtStart;
        if (timeDelta >= config.timeToEndTicks) {
          endInvasion();
          return true;
        }
        break;
    }
    return false;
  }

  private static List<BlockPos> getNeighborOffsets() {
    List<BlockPos> positions =
        Arrays.asList(
            new BlockPos(0, -1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 1, 0));
    Collections.shuffle(positions);
    return positions;
  }

  private static BlockPos castDownBlockPos(World worldIn, BlockPos pos) {
    while (worldIn.getBlockState(pos).getBlock() == Blocks.AIR) pos = pos.add(0, -1, 0);
    return pos;
  }
}
