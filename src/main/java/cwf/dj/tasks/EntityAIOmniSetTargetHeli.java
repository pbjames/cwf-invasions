package cwf.dj.tasks;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIOmniSetTargetHeli<T extends EntityLivingBase> extends EntityAIBase {
  private final EntityLiving entityLiving;
  protected final T targetEntity;

  public EntityAIOmniSetTargetHeli(EntityLiving creature, T target) {
    this.entityLiving = creature;
    this.targetEntity = target;
  }

  @Override
  public boolean shouldContinueExecuting() {
    return this.shouldExecute();
  }

  @Override
  public boolean shouldExecute() {
    return this.entityLiving.getAttackTarget() != targetEntity;
  }

  @Override
  public void startExecuting() {
    this.entityLiving.setAttackTarget(this.targetEntity);
    super.startExecuting();
  }

  @Override
  public void resetTask() {}
}
