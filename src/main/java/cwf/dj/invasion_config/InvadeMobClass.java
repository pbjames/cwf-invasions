package cwf.dj.invasion_config;

public class InvadeMobClass {
  public final String ent;
  public final int weight;
  public final InvadeMobType type;

  public InvadeMobClass(String ent, int weight, InvadeMobType type) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
  }

  public InvadeMobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = InvadeMobType.CQC;
  }
}
