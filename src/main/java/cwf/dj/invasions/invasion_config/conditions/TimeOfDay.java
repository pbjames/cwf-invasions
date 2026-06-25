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

  public static TimeOfDay of(long dayTime) {
    TimeOfDay closest = SUNRISE;
    long minDiff = Long.MAX_VALUE;
    for (TimeOfDay dt : values()) {
      long diff = Math.abs(dt.ticks - dayTime);
      if (diff < minDiff) {
        minDiff = diff;
        closest = dt;
      }
    }
    return closest;
  }
}
