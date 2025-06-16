package cwf.dj.invasion_config;

public class InvadeMobClass {
  public final String ent;
  public final InvadeMobType type;
  public final int weight;
  public final int yOffset;
  public final int minDist;
  public final int maxDist;

  public InvadeMobClass(
      String ent, int weight, InvadeMobType type, int yOffset, int minDist, int maxDist) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
    this.yOffset = yOffset;
    this.minDist = minDist;
    this.maxDist = maxDist;
  }

  public InvadeMobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = InvadeMobType.CQC;
    this.yOffset = 0;
    this.minDist = -1;
    this.maxDist = -1;
  }
}
