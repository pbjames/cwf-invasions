package cwf.dj.invasions.invasion_config.conditions;

import cwf.dj.invasions.integrations.GameStagesCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartCondition {
  public final StartConditionType type = StartConditionType.TIME_OF_DAY;
  public final TimeOfDay timeOfDayToStart = TimeOfDay.NIGHT;
  public final String dimensionToStart = "";
  public final String gameStageToStart = "";
  public final int nDaysToStart = 7;
  public final long cooldownTicks = 0;

  public boolean shouldStart(long worldTime, long lastInvasionTime, ServerPlayer player) {
    boolean inDimension =
        player.level().dimension().location().equals(new ResourceLocation(dimensionToStart));
    boolean hasGameStage = GameStagesCompat.hasStage(player, gameStageToStart);
    if (!dimensionToStart.isEmpty() && !inDimension) return false;
    if (!gameStageToStart.isEmpty() && !hasGameStage) return false;
    if (cooldownTicks > 0 && (worldTime - lastInvasionTime) <= cooldownTicks) return false;
    return switch (type) {
      case TIME_OF_DAY -> TimeOfDay.of(worldTime) == timeOfDayToStart;
      case EVERY_N_DAYS -> (worldTime / 24000) % nDaysToStart == 0;
      case GAMESTAGE -> hasGameStage;
      case DIMENSION -> inDimension;
      case NEVER -> false;
    };
  }
}
