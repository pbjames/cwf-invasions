package cwf.dj.invasions.invasion_config;

import cwf.dj.invasions.Configuration;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;

public enum InvadeMobType {
  CQC,
  GUNNER,
  MINER;

  public void applySpecialTasks(PathfinderMob creature) {
    switch (this) {
      case CQC:
        break;
      case GUNNER:
        break;
      case MINER:
        if (Configuration.COMMON.minersGlow.get())
          creature.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000, 0));
        break;
    }
  }
}
