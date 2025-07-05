package cwf.dj.invasion_config;

import cwf.dj.Configuration;
import cwf.dj.tasks.EntityAIMineToTarget;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public enum InvadeMobType {
  CQC,
  GUNNER,
  MINER;

  public void applySpecialTasks(EntityCreature creature, EntityPlayerMP player) {
    switch (this) {
      case CQC:
        break;
      case GUNNER:
        break;
      case MINER:
        creature.targetTasks.addTask(
            1, new EntityAIMineToTarget<EntityPlayerMP>(creature, 1, player));
        if (Configuration.common.minersGlow)
          creature.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 10000, 0));
        break;
    }
  }
}
