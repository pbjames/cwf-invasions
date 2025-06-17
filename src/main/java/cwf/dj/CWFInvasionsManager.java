package cwf.dj;

import cwf.dj.invasion_config.InvadeMobClass;
import cwf.dj.invasion_config.InvasionConfig;
import cwf.dj.invasion_config.InvasionConfigCollection;
import java.util.ArrayList;
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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
  private static ManagerDataStore data;
  public static List<Entity> activeMobs = new ArrayList<>();
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
  public static World homeWorld = DimensionManager.getWorld(0);

  @SubscribeEvent
  public static void onTickServer(ServerTickEvent event) {
    if (event.phase != Phase.START) return;
    if (homeWorld == null) return;
    if (data == null) return;
    int slowCheckTime = Configuration.common.slowTickTime;
    int fastCheckTime = Configuration.common.fastTickTime;
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    if (homeWorld.getTotalWorldTime() % fastCheckTime == 0)
      for (EntityPlayerMP player : players) if (getInvasion()) invade(player, players.size());
    if (homeWorld.getTotalWorldTime() % slowCheckTime == 0) {
      players.forEach(player -> checkSetInvasion(player));
    }
  }

  public static void refreshWorld(World worldIn) {
    homeWorld = worldIn;
  }

  public static InvasionConfig getChosenConfig() {
    // TODO: reverse this later
    return reverseMap(InvasionConfigCollection.configs).get(data.configName);
  }

  public static void createAndStoreData(World worldIn) {
    // INFO: Assume if we have no world data, there's no invasion history
    ManagerDataStore data = new ManagerDataStore();
    worldIn.getMapStorage().setData(ManagerDataStore.NAME, data);
    data.markDirty();
    setData(data);
  }

  public static void setData(ManagerDataStore dataIn) {
    data = dataIn;
  }

  public static void loadFromWorld(World worldIn) {
    ManagerDataStore dataFrom = ManagerDataStore.retrieveData(worldIn);
    setData(dataFrom);
  }

  public static void logConfig() {
    LOGGER.info(
        "Log state:"
            + " CWFINvasionsManager[invasion={},timeAtStart={},slainSinceStart={},configName={}]",
        data.invasionHappeningNow,
        data.timeAtStart,
        data.slainSinceStart,
        data.configName);
  }

  public static <K, V> Map<V, K> reverseMap(Map<K, V> original) {
    Map<V, K> reversed = new HashMap<>();
    for (Map.Entry<K, V> entry : original.entrySet())
      reversed.put(entry.getValue(), entry.getKey());
    return reversed;
  }

  public static void spawnFromConfig(EntityPlayerMP player) {
    InvadeMobClass mobClass = getChosenConfig().pickRandomMobClass();
    double healthScale = getChosenConfig().getHealthFactor();
    double damageScale = getChosenConfig().getDamageFactor();
    int minDistMob =
        mobClass.minDist == -1 ? Configuration.common.minInvadeDistance : mobClass.minDist;
    int maxDistMob =
        mobClass.maxDist == -1 ? Configuration.common.maxInvadeDistance : mobClass.maxDist;
    BlockPos spawnPos = findAirBlockNear(player, minDistMob, maxDistMob);
    Entity spawnedEntity = mobClass.spawn(player, spawnPos, healthScale, damageScale);
    activeMobs.add(spawnedEntity);
  }

  public static void removeUnaccountedMobs() {
    int oldCount = activeMobs.size();
    activeMobs =
        activeMobs.stream()
            .filter(mob -> !mob.isDead || mob.isAddedToWorld())
            .collect(Collectors.toList());
    data.slainSinceStart += oldCount - activeMobs.size();
    data.markDirty();
  }

  public static void checkSetInvasion(@Nonnull EntityPlayerMP player) {
    if (getInvasion()) {
      checkForEnding(getChosenConfig());
    } else {
      // INFO: For multiple invasions we pick the first match for start conditions
      // and stick with that for the end condition
      // Perhaps in the future, we could manager multiple invasions simultaneously
      for (InvasionConfig config : InvasionConfigCollection.configs.keySet()) {
        if (checkForStarting(config)) {
          data.configName = InvasionConfigCollection.configs.get(config);
          data.markDirty();
          return;
        }
      }
    }
  }

  private static boolean checkForStarting(InvasionConfig config) {
    switch (config.startCondition) {
      case FORTNIGHT:
        if (homeWorld.getTotalWorldTime() % (14 * 24000) == 0) {
          startInvasion();
          return true;
        }
        break;
      case FULL_MOON:
        if (homeWorld.getMoonPhase() == 0) {
          startInvasion();
          return true;
        }
        break;
      case NIGHT:
        if (homeWorld.getWorldTime() % 13000 == 0) {
          startInvasion();
          return true;
        }
        break;
      case DAY:
        if (homeWorld.getWorldTime() == 0) {
          startInvasion();
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
        if (data.slainSinceStart * players.size() >= config.mobCountToEnd) {
          endInvasion();
          return true;
        }
        break;
      case TIME:
        long timeDelta = homeWorld.getTotalWorldTime() - data.timeAtStart;
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
    removeUnaccountedMobs();
    if (activeMobs.size() >= (maintenanceLevel * playerCount)) return;
    spawnFromConfig(player);
  }

  public static BlockPos findAirBlockNear(EntityPlayer player, int minDist, int maxDist) {
    int maxDistSq = maxDist * maxDist;
    int minDistSq = minDist * minDist;
    int randomX = (RANDOM.nextBoolean() ? -1 : 1) * RANDOM.nextInt(maxDist - minDist) + minDist;
    int randomZ = (RANDOM.nextBoolean() ? -1 : 1) * RANDOM.nextInt(maxDist - minDist) + minDist;
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

  private static BlockPos castDownBlockPos(World world, BlockPos pos) {
    while (world.getBlockState(pos).getBlock() == Blocks.AIR) pos = pos.add(0, -1, 0);
    return pos;
  }

  public static void startInvasion() {
    LOGGER.info("Invasion starting");
    List<EntityPlayerMP> players = SERVER.getPlayerList().getPlayers();
    players.forEach(player -> player.sendMessage(INVASION_STARTED_MSG));
    data.invasionHappeningNow = true;
    data.slainSinceStart = 0;
    data.timeAtStart = homeWorld.getTotalWorldTime();
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
}
