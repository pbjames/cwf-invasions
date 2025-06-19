package cwf.dj.tasks;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOmniSetTarget<T extends EntityLivingBase> extends EntityAITarget {
  protected final T targetEntity;

  public EntityAIOmniSetTarget(EntityCreature creature, T target) {
    super(creature, false, false);
    this.targetEntity = target;
  }

  @Override
  public boolean shouldContinueExecuting() {
    return this.shouldExecute();
  }

  @Override
  public boolean shouldExecute() {
    return this.taskOwner.getAttackTarget() != target;
  }

  @Override
  public void startExecuting() {
    this.taskOwner.setAttackTarget(this.targetEntity);
    super.startExecuting();
  }

  @Override
  protected double getTargetDistance() {
    return 160.0D;
  }

  @Override
  public void resetTask() {}

  @Override
  protected boolean isSuitableTarget(
      @Nullable EntityLivingBase target, boolean includeInvincibles) {
    return target != null;
  }
}
