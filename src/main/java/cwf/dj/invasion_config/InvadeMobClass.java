package cwf.dj.invasion_config;

import cwf.dj.tasks.EntityAIChaseMelee;
import cwf.dj.tasks.EntityAIOmniSetTarget;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class InvadeMobClass {
  public final String ent;
  public final InvadeMobType type;
  public final int weight;
  public final int yOffset;
  public final int minDist;
  public final int maxDist;

  public InvadeMobClass(
      String ent, int weight, InvadeMobType type, int yOffset, int minDist, int maxDist) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
    this.yOffset = yOffset;
    this.minDist = minDist;
    this.maxDist = maxDist;
  }

  public InvadeMobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = InvadeMobType.CQC;
    this.yOffset = 0;
    this.minDist = -1;
    this.maxDist = -1;
  }

  @Nullable
  public Entity spawn(
      EntityPlayerMP player, BlockPos spawnPos, double healthScale, double damageScale) {
    EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(ent));
    try {
      Entity realEntity =
          entry.getEntityClass().getConstructor(World.class).newInstance(player.world);
      if (realEntity instanceof EntityLivingBase)
        applyScaledAttributes((EntityLivingBase) realEntity, healthScale, damageScale);
      if (realEntity instanceof EntityCreature)
        prepareEntityCreature((EntityCreature) realEntity, player, this);
      realEntity.setPosition(spawnPos.getX(), spawnPos.getY() + yOffset, spawnPos.getZ());
      player.world.spawnEntity(realEntity);
      return realEntity;
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static void applyScaledAttributes(
      EntityLivingBase ent, double healthScale, double damageScale) {
    IAttributeInstance health = ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    IAttributeInstance damage = ent.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
    IAttributeInstance speed = ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
    IAttributeInstance armor = ent.getEntityAttribute(SharedMonsterAttributes.ARMOR);
    if (health != null) health.setBaseValue(health.getBaseValue() * healthScale);
    if (damage != null) damage.setBaseValue(damage.getBaseValue() * damageScale);
    if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.05);
    if (armor != null) armor.setBaseValue(armor.getBaseValue() * 1.10);
  }

  private static void prepareEntityCreature(
      EntityCreature creature, EntityPlayerMP player, InvadeMobClass mobClass) {
    creature.tasks.addTask(2, new EntityAIOmniSetTarget<EntityPlayerMP>(creature, player));
    // creature.tasks.addTask(2, new EntityAIChaseMelee<EntityPlayerMP>(creature, 1.0D, player));
    creature.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
  }
}
