package cwf.dj.invasion_config;

public class InvadeMobClass {
  public final String ent;
  public final InvadeMobType type;
  public final int weight;
  public final int yOffset;

  public InvadeMobClass(String ent, int weight, InvadeMobType type, int yOffset) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
    this.yOffset = yOffset;
  }

  public InvadeMobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = InvadeMobType.CQC;
    this.yOffset = 0;
  }
}
