package cwf.dj.invasions;

import cwf.dj.invasions.integrations.GameStagesCompat;
import cwf.dj.invasions.invasion_config.InvadeMobClass;
import cwf.dj.invasions.invasion_config.InvasionConfig;
import cwf.dj.invasions.invasion_config.InvasionConfigCollection;
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
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = Invasions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InvasionsManager {
  private static final int DAY_TICKS = 24000;
  private static final int NIGHT_START = 13000;
  private static final int NIGHT_FINISH = 23000;
  private static ManagerDataStore data;
  public static Set<Entity> activeMobs = new HashSet<>();
  public static final Random RANDOM = new Random();
  public static final Logger LOGGER = Invasions.LOGGER;
  public static final Component INVASION_STARTED_MSG = Component.literal("§l§cINVASION STARTED§r");
  public static final Component INVASION_ENDED_MSG = Component.literal("§l§6INVASION ENDED§r");
  public static ServerLevel level;

  @SubscribeEvent
  public static void onTickServer(ServerTickEvent event) {
    if (event.phase != Phase.START) return;
    if (level == null) return;
    if (data == null) return;
    // TODO: Use playerIDs and avoid using direct player references because
    // they can leave the game
    List<ServerPlayer> players = level.getPlayers((player) -> true);
    if (level.getGameTime() % Configuration.COMMON.fastTickTime.get() == 0)
      for (ServerPlayer player : players)
        if (data.invasionHappeningNow) invade(player, players.size());
    if (level.getGameTime() % Configuration.COMMON.slowTickTime.get() == 0) {
      players.forEach(player -> checkSetInvasion(player));
    }
    if (level.getGameTime() % 20 == 0) LOGGER.info("Active mobs: {}", activeMobs);
  }

  @SubscribeEvent
  public static void onMobDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!activeMobs.contains(entity)) return;
    activeMobs.remove(entity);
    // DamageSource source = event.getSource();
    data.incrementSlain();
  }

  public static void loadServerLevelData(ServerLevel levelIn) {
    ManagerDataStore data = ManagerDataStore.get(levelIn);
    setData(data);
    setLevel(levelIn);
  }

  private static void startInvasion(InvasionConfig config) {
    LOGGER.info("Invasion starting");
    List<ServerPlayer> players = level.getPlayers((player) -> true);
    players.forEach(player -> player.sendSystemMessage(INVASION_STARTED_MSG));
    data.setNewInvasion(level.getGameTime());
  }

  private static void endInvasion(InvasionConfig config) {
    LOGGER.info("Invasion ending");
    List<ServerPlayer> players = level.getPlayers((player) -> true);
    for (ServerPlayer player : players) {
      player.sendSystemMessage(INVASION_ENDED_MSG);
      if (!config.gameStageAwarded.isEmpty()) {
        GameStagesCompat.addStage(player, config.gameStageAwarded);
      }
    }
    data.setEndInvasion();
  }

  private static void invade(ServerPlayer player, int playerCount) {
    int maintenanceLevel = getChosenConfig().maintainedPopulation;
    activeMobs =
        activeMobs.stream().filter(mob -> mob.isAddedToWorld()).collect(Collectors.toSet());
    if (activeMobs.size() >= (maintenanceLevel * playerCount)) return;
    spawnFromConfig(player);
  }

  @Nullable
  private static InvasionConfig getChosenConfig() {
    return InvasionConfigCollection.configs.get(data.configName);
  }

  private static BlockPos findAirBlockNear(Player player, int minDist, int maxDist) {
    int maxDistSq = maxDist * maxDist;
    int minDistSq = minDist * minDist;
    int distDiff = maxDist - minDist;
    int randomX = RANDOM.nextInt(2 * distDiff) - distDiff + minDist;
    int randomZ = RANDOM.nextInt(2 * distDiff) - distDiff + minDist;
    BlockPos start = player.blockPosition();
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new LinkedList<>();
    BlockPos randomPos = new BlockPos(start.offset(randomX, (int) player.getEyeHeight(), randomZ));
    LOGGER.info("Random pos: {}", randomPos);
    queue.add(randomPos);
    visited.add(start);
    while (!queue.isEmpty()) {
      BlockPos pos = queue.poll();
      BlockPos adjustedPos = castDownBlockPos(player.level(), pos);
      BlockState state = player.level().getBlockState(pos);
      double distFromStart = start.distSqr(adjustedPos);
      if (state.getBlock().equals(Blocks.AIR)
          && (distFromStart > minDistSq)
          && (distFromStart < maxDistSq))
        return adjustedPos.offset(0, (int) player.getEyeHeight(), 0);
      for (BlockPos offset : getNeighborOffsets()) {
        BlockPos neighbor = pos.offset(offset);
        if (!visited.contains(neighbor)) {
          visited.add(neighbor);
          queue.add(neighbor);
        }
      }
    }
    LOGGER.info("Position not satisfied for spawning, being aggressive");
    return player.blockPosition();
  }

  private static void spawnFromConfig(ServerPlayer player) {
    InvadeMobClass mobClass = getChosenConfig().pickRandomMobClass();
    double healthScale = getChosenConfig().getHealthFactor();
    double damageScale = getChosenConfig().getDamageFactor();
    BlockPos spawnPos = findAirBlockNear(player, mobClass.minDist, mobClass.maxDist);
    Entity spawnedEntity = mobClass.spawn(player, spawnPos, healthScale, damageScale);
    activeMobs.add(spawnedEntity);
  }

  private static void checkSetInvasion(@Nonnull ServerPlayer player) {
    if (data.invasionHappeningNow) {
      InvasionConfig config = getChosenConfig();
      if (checkForEnding(config)) endInvasion(config);
    } else {
      // INFO: For multiple invasions we pick the first match for start conditions
      // and stick with that for the end condition
      // Perhaps in the future, we could manager multiple invasions simultaneously
      for (InvasionConfig config : InvasionConfigCollection.configs.values()) {
        if (checkForStarting(player, config)) {
          startInvasion(config);
          data.configName = reverseMap(InvasionConfigCollection.configs).get(config);
          data.setDirty();
          return;
        }
      }
    }
  }

  private static void setLevel(ServerLevel levelIn) {
    level = levelIn;
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

  private static boolean checkForStarting(ServerPlayer player, InvasionConfig config) {
    long currentTime = level.getGameTime();
    // INFO: 0 -> stand-in value for whatever the current world time is when we load in
    if (data.cooldownTimeStamp == 0) data.cooldownTimeStamp = currentTime;
    LOGGER.info("Checking for invasion start");
    long ticksSinceCooldown = currentTime - data.cooldownTimeStamp;
    if (ticksSinceCooldown < config.cooldownTicks) {
      LOGGER.info(
          "Cooldown active, not invading: {} - {} < {}",
          level.getGameTime(),
          data.cooldownTimeStamp,
          config.cooldownTicks);
      // return false;
    }
    // TODO: Update to use tag system
    // if (config.dimensionRequired != -1
    //    && config.dimensionRequired != player.world.provider.getDimension()) return false;
    if (!config.gameStageRequired.isEmpty()
        && !GameStagesCompat.hasStage(player, config.gameStageRequired)) return false;
    long gameTime = level.getGameTime();
    long time = level.getDayTime();
    switch (config.startCondition) {
      case FORTNIGHT:
        if (gameTime % (14 * DAY_TICKS) == 0) return true;
        break;
      case FULL_MOON:
        if (level.getMoonPhase() == 0) return true;
        break;
      case NIGHT:
        if (NIGHT_FINISH > time && time > NIGHT_START) return true;
        break;
      case DAY:
        if (time == 0) return true;
        break;
      case NEVER:
        break;
    }
    return false;
  }

  private static boolean checkForEnding(InvasionConfig config) {
    List<ServerPlayer> players = level.getPlayers((player) -> true);
    switch (config.endingCondition) {
      case MOBCOUNT:
        if (data.slainSinceStart / players.size() >= config.mobCountToEnd) return true;
        break;
      case TIME:
        long timeDelta = level.getGameTime() - data.timeAtStart;
        if (timeDelta >= config.timeToEndTicks) return true;
        break;
      case NEVER:
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

  private static BlockPos castDownBlockPos(Level levelIn, BlockPos pos) {
    while (levelIn.getBlockState(pos).getBlock() == Blocks.AIR) pos = pos.offset(0, -1, 0);
    return pos;
  }
}
