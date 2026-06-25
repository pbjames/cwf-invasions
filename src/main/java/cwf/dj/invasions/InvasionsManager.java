package cwf.dj.invasions;

import cwf.dj.invasions.integrations.GameStagesCompat;
import cwf.dj.invasions.invasion_config.InvasionConfig;
import cwf.dj.invasions.invasion_config.InvasionConfigCollection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
  private static ManagerDataStore data;
  public static Set<Entity> mobs = new HashSet<>();
  public static final Set<UUID> players = new HashSet<>();
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
    if (level.getGameTime() % Configuration.COMMON.fastTickTime.get() != 0
        || level.getGameTime() % Configuration.COMMON.slowTickTime.get() != 0) return;

    if (level.getGameTime() % Configuration.COMMON.fastTickTime.get() == 0) {
      players.addAll(
          level.getPlayers(p -> true).stream().map(p -> p.getUUID()).collect(Collectors.toSet()));
      for (UUID player : players) if (data.invasionHappeningNow) invade(player, players.size());
    }
    if (level.getGameTime() % Configuration.COMMON.slowTickTime.get() == 0) {
      players.forEach(player -> checkSetInvasion(player));
      players.removeIf(u -> resolvePlayer(u) == null);
    }
    if (Configuration.COMMON.debug.get()) Invasions.LOGGER.info("Active mobs: " + mobs.toString());
  }

  @SubscribeEvent
  public static void onMobDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!mobs.contains(entity)) return;
    mobs.remove(entity);
    data.incrementSlain();
  }

  public static ServerPlayer resolvePlayer(UUID playerUUID) {
    return level.getServer().getPlayerList().getPlayer(playerUUID);
  }

  public static void loadServerLevelData(ServerLevel levelIn) {
    ManagerDataStore data = ManagerDataStore.get(levelIn);
    setData(data);
    setLevel(levelIn);
  }

  public static ManagerDataStore getData() {
    return data;
  }

  public static void startInvasion(InvasionConfig config) {
    players.stream()
        .map(InvasionsManager::resolvePlayer)
        .filter(Objects::nonNull)
        .forEach(
            player -> {
              player.sendSystemMessage(INVASION_STARTED_MSG);
              player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 65535, 0));
            });
    data.setNewInvasion(level.getGameTime());
  }

  public static void endInvasion(InvasionConfig config) {
    players.stream()
        .map(InvasionsManager::resolvePlayer)
        .filter(Objects::nonNull)
        .forEach(
            player -> {
              player.sendSystemMessage(INVASION_ENDED_MSG);
              player.removeEffect(MobEffects.GLOWING);
              if (!config.gameStageAwarded.isEmpty())
                GameStagesCompat.addStage(player, config.gameStageAwarded);
            });
    data.setEndInvasion();
  }

  private static void invade(UUID playerUUID, int playerCount) {
    int maintenanceLevel = getChosenConfig().maintainedPopulation;
    mobs = mobs.stream().filter(mob -> mob.isAddedToWorld()).collect(Collectors.toSet());
    if (mobs.size() >= (maintenanceLevel * playerCount)) return;
    spawnFromConfig(level.getServer().getPlayerList().getPlayer(playerUUID));
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
    return player.blockPosition();
  }

  private static void spawnFromConfig(ServerPlayer player) {
    cwf.dj.invasions.invasion_config.mobs.MobClass mobClass =
        getChosenConfig().pickRandomMobClass();
    double healthScale = getChosenConfig().getHealthFactor();
    double damageScale = getChosenConfig().getDamageFactor();
    BlockPos spawnPos = findAirBlockNear(player, mobClass.minDist, mobClass.maxDist);
    Entity spawnedEntity = mobClass.spawn(player, spawnPos, healthScale, damageScale);
    mobs.add(spawnedEntity);
  }

  private static void checkSetInvasion(UUID player) {
    if (data.invasionHappeningNow) {
      InvasionConfig config = getChosenConfig();
      if (checkForEnding(config)) endInvasion(config);
    } else {
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

  private static boolean checkForStarting(UUID playerUUID, InvasionConfig config) {
    long gameTime = level.getGameTime();
    long dayTime = level.getDayTime();
    data.checkNewGameCooldown(gameTime);
    ServerPlayer player;
    if ((player = resolvePlayer(playerUUID)) != null)
      return config.startCondition.shouldStart(gameTime, dayTime, data.cooldownTimeStamp, player);
    return false;
  }

  private static boolean checkForEnding(InvasionConfig config) {
    long gameTime = level.getGameTime();
    long dayTime = level.getDayTime();
    int slainEach = data.slainSinceStart / players.size();
    return players.stream()
        .map(InvasionsManager::resolvePlayer)
        .filter(Objects::nonNull)
        .anyMatch(player -> config.endingCondition.shouldEnd(gameTime, dayTime, slainEach, player));
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
