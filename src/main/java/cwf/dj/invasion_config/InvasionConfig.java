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
  public InvadeMobClass[] mobClasses = {
    new InvadeMobClass("minecraft:zombie", 5, InvadeMobType.CQC, 0, -1, -1),
    new InvadeMobClass("minecraft:skeleton", 2, InvadeMobType.SUPPORT, 0, -1, -1),
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

  public int totalAttributeWeights() {
    return healthScalingWeight + damageScalingWeight;
  }

  public double getHealthFactor() {
    return (healthScalingWeight / totalAttributeWeights()) * totalMobScalingFactor;
  }

  public double getDamageFactor() {
    return (healthScalingWeight / totalAttributeWeights()) * totalMobScalingFactor;
  }
}
