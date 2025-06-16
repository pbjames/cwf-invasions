package cwf.dj.tasks;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityAIChaseMelee<T extends EntityLivingBase> extends EntityAIBase {
  World world;
  protected EntityCreature attacker;

  /** An amount of decrementing ticks that allows the entity to attack once the tick reaches 0. */
  protected int attackTick;

  /** The speed with which the mob will approach the target */
  double speedTowardsTarget;

  /**
   * When true, the mob will continue chasing its target, even if it can't find a path to them right
   * now.
   */
  boolean longMemory;

  /** The PathEntity of our entity. */
  Path path;

  private double targetX;
  private double targetY;
  private double targetZ;
  protected final int attackInterval = 20;
  private T targetEntity;

  public EntityAIChaseMelee(EntityCreature creature, double speedIn, T target) {
    this.attacker = creature;
    this.world = creature.world;
    this.speedTowardsTarget = speedIn;
    this.longMemory = true;
    this.targetEntity = target;
    this.setMutexBits(3);
  }

  /** Returns whether the EntityAIBase should begin execution. */
  public boolean shouldExecute() {
    if (targetEntity == null) {
      return false;
    } else if (!targetEntity.isEntityAlive()) {
      return false;
    } else {
      this.path = this.attacker.getNavigator().getPathToEntityLiving(targetEntity);
      if (this.path != null) {
        return true;
      } else {
        return this.getAttackReachSqr(targetEntity)
            >= this.attacker.getDistanceSq(
                targetEntity.posX, targetEntity.getEntityBoundingBox().minY, targetEntity.posZ);
      }
    }
  }

  /** Returns whether an in-progress EntityAIBase should continue executing */
  public boolean shouldContinueExecuting() {
    if (targetEntity == null) {
      return false;
    } else if (targetEntity.isEntityAlive()) {
      return true;
    } else {
      return true;
    }
  }

  /** Execute a one shot task or start executing a continuous task */
  public void startExecuting() {
    this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
  }

  /** Reset the task's internal state. Called when this task is interrupted by another one */
  public void resetTask() {
    if (targetEntity instanceof EntityPlayer
        && (((EntityPlayer) targetEntity).isSpectator()
            || ((EntityPlayer) targetEntity).isCreative())) {
      this.attacker.setAttackTarget((EntityLivingBase) null);
    }
    //this.attacker.getNavigator().clearPath();
  }

  /** Keep ticking a continuous task that has already been started */
  public void updateTask() {
    this.attacker.getLookHelper().setLookPositionWithEntity(targetEntity, 30.0F, 30.0F);
    double d0 =
        this.attacker.getDistanceSq(
            targetEntity.posX, targetEntity.getEntityBoundingBox().minY, targetEntity.posZ);

    this.targetX = targetEntity.posX;
    this.targetY = targetEntity.getEntityBoundingBox().minY;
    this.targetZ = targetEntity.posZ;

    this.attacker.getNavigator().tryMoveToEntityLiving(targetEntity, this.speedTowardsTarget);

    this.attackTick = Math.max(this.attackTick - 1, 0);
    this.checkAndPerformAttack(targetEntity, d0);
  }

  protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
    double d0 = this.getAttackReachSqr(enemy);

    if (distToEnemySqr <= d0 && this.attackTick <= 0) {
      this.attackTick = 20;
      this.attacker.swingArm(EnumHand.MAIN_HAND);
      this.attacker.attackEntityAsMob(enemy);
    }
  }

  protected double getAttackReachSqr(EntityLivingBase attackTarget) {
    return (double) (this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width);
  }
}
