package cwf.dj.invasions.invasion_config;

import cwf.dj.invasions.Configuration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;

public enum InvadeMobType {
  CQC,
  GUNNER,
  MINER;

  public void applySpecialTasks(PathfinderMob creature, ServerPlayer player) {
    switch (this) {
      case CQC:
        break;
      case GUNNER:
        break;
      case MINER:
        // creature.targetTasks.addTask(
        //    1, new EntityAIMineToTarget<EntityPlayerMP>(creature, 1, player));
        if (Configuration.COMMON.minersGlow.get())
          creature.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000, 0));
        break;
    }
  }
}
