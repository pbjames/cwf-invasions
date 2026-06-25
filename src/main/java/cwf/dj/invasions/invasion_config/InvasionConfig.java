package cwf.dj.invasions.invasion_config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cwf.dj.invasions.invasion_config.conditions.EndCondition;
import cwf.dj.invasions.invasion_config.conditions.StartCondition;
import cwf.dj.invasions.invasion_config.mobs.MobClass;
import cwf.dj.invasions.invasion_config.mobs.MobType;

public class InvasionConfig {
  public StartCondition startCondition = new StartCondition();
  public EndCondition endingCondition = new EndCondition();
  public int maintainedPopulation = 10;
  public int healthScalingWeight = 2;
  public int damageScalingWeight = 1;
  public int totalMobScalingFactor = 2;
  public String gameStageAwarded = "";
  public List<MobClass> mobClasses =
      new ArrayList<>(
          Arrays.asList(
              new MobClass[] {
                new MobClass("minecraft:zombie", 5, MobType.CQC, 0, 16, 32),
                new MobClass("minecraft:skeleton", 1, MobType.MINER, 0, 30, 40)
              }));

  public MobClass pickRandomMobClass() {
    int totalWeight = 0;
    for (MobClass mobClass : mobClasses) totalWeight += mobClass.weight;
    int selected = new Random().nextInt(totalWeight);
    int runSum = 0;
    for (MobClass mobClass : mobClasses) {
      runSum += mobClass.weight;
      if ((selected < runSum) && (selected >= (runSum - mobClass.weight))) return mobClass;
    }
    return mobClasses.get(mobClasses.size() - 1);
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
