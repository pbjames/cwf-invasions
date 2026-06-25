package cwf.dj.invasions.invasion_config.conditions;

import cwf.dj.invasions.integrations.GameStagesCompat;
import net.minecraft.server.level.ServerPlayer;

public class EndCondition {
  public final EndConditionType type = EndConditionType.TIME_OF_DAY;
  public TimeOfDay timeOfDayToEnd = TimeOfDay.SUNRISE;
  public int nDaysToEnd = 3;
  public int mobsKilledToEnd = 100;
  public String gamestageToEnd = "";

  public boolean shouldEnd(long worldTime, long dayTime, int mobsKilled, ServerPlayer player) {
    return switch (type) {
      case MOBS_KILLED -> mobsKilled >= mobsKilledToEnd;
      case TIME_OF_DAY -> TimeOfDay.of(dayTime) == timeOfDayToEnd;
      case AFTER_N_DAYS -> (worldTime / 24000) % nDaysToEnd == 0;
      case GAMESTAGE -> GameStagesCompat.hasStage(player, gamestageToEnd);
      case NEVER -> false;
    };
  }
}
