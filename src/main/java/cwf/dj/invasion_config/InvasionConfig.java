package cwf.dj.invasion_config;

import java.util.Random;

public class InvasionConfig {
  public InvadeStartCondition startCondition = InvadeStartCondition.NIGHT;
  public InvadeEndCondition endingCondition = InvadeEndCondition.MOBCOUNT;
  public int mobCountToEnd = 120;
  public int timeToEndTicks = 300 * 20;
  public int maintainedPopulation = 10;
  public int healthScalingWeight = 2;
  public int damageScalingWeight = 1;
  public int totalMobScalingFactor = 2;
  public long cooldownTicks = 13000;
  public long __cooldownTSDontChangeMePlz = 0;
  public int dimensionRequired = 0;
  public String gameStageRequired = "";
  public String gameStageAwarded = "";
  public InvadeMobClass[] mobClasses = {
    new InvadeMobClass("minecraft:zombie", 5, InvadeMobType.CQC, 0, 16, 32),
    new InvadeMobClass("minecraft:skeleton", 1, InvadeMobType.MINER, 0, 30, 40),
  };

  public InvadeMobClass pickRandomMobClass() {
    int totalWeight = 0;
    for (InvadeMobClass mobClass : mobClasses) totalWeight += mobClass.weight;
    int selected = new Random().nextInt(totalWeight);
    int runSum = 0;
    for (InvadeMobClass mobClass : mobClasses) {
      runSum += mobClass.weight;
      if ((selected < runSum) && (selected >= (runSum - mobClass.weight))) return mobClass;
    }
    return mobClasses[mobClasses.length - 1];
  }

  public double getHealthFactor() {
    return 1 + ((healthScalingWeight / totalAttributeWeights()) * totalMobScalingFactor);
  }

  public double getDamageFactor() {
    return 1 + ((damageScalingWeight / totalAttributeWeights()) * totalMobScalingFactor);
  }

  private int totalAttributeWeights() {
    return healthScalingWeight + damageScalingWeight;
  }
}
