package cwf.dj.tasks;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIOmniSetTarget<T extends EntityLivingBase> extends EntityAITarget {
  protected final T targetEntity;

  public EntityAIOmniSetTarget(
      EntityCreature creature, T target) {
    super(creature, false, false);
    this.targetEntity = target;
  }

  public boolean shouldExecute() {
    return this.taskOwner.getAttackTarget() != target;
  }

  public void startExecuting() {
    this.taskOwner.setAttackTarget(this.targetEntity);
    super.startExecuting();
  }
}
