package cwf.dj.invasions.invasion_config.conditions;

public enum TimeOfDay {
  SUNRISE(0),
  NOON(6000),
  SUNSET(12000),
  NIGHT(13000),
  MIDNIGHT(18000);

  public final long ticks;

  TimeOfDay(long ticks) {
    this.ticks = ticks;
  }

  public static TimeOfDay of(long time) {
    long timeOfDay = time % 24000;
    TimeOfDay closest = SUNRISE;
    long minDiff = Long.MAX_VALUE;
    for (TimeOfDay dt : values()) {
      long diff = Math.abs(dt.ticks - timeOfDay);
      if (diff < minDiff) {
        minDiff = diff;
        closest = dt;
      }
    }
    return closest;
  }
}
