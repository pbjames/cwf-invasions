package cwf.dj.invasions.invasion_config.mobs;

import cwf.dj.invasions.Configuration;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

public class MobClass {
  public final String ent;
  public final MobType type;
  public final int weight;
  public final int yOffset;
  public final int minDist;
  public final int maxDist;

  public MobClass(String ent, int weight, MobType type, int yOffset, int minDist, int maxDist) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
    this.yOffset = yOffset;
    this.minDist = minDist;
    this.maxDist = maxDist;
  }

  public MobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = MobType.CQC;
    this.yOffset = 0;
    this.minDist = 32;
    this.maxDist = 64;
  }

  @Nullable
  public Entity spawn(
      ServerPlayer player, BlockPos spawnPos, double healthScale, double damageScale) {
    EntityType<?> entry = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(ent));
    try {
      Entity realEntity = entry.create(player.level());
      if (realEntity instanceof LivingEntity)
        applyScaledAttributes((LivingEntity) realEntity, healthScale, damageScale);
      if (realEntity instanceof PathfinderMob)
        prepareEntityCreature((PathfinderMob) realEntity, player);
      realEntity.setPos(spawnPos.getX(), spawnPos.getY() + yOffset, spawnPos.getZ());
      player.level().addFreshEntity(realEntity);
      return realEntity;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static void applyScaledAttributes(
      LivingEntity ent, double healthScale, double damageScale) {
    AttributeInstance health = ent.getAttribute(Attributes.MAX_HEALTH);
    AttributeInstance damage = ent.getAttribute(Attributes.ATTACK_DAMAGE);
    AttributeInstance speed = ent.getAttribute(Attributes.MOVEMENT_SPEED);
    AttributeInstance armor = ent.getAttribute(Attributes.ARMOR);
    AttributeInstance range = ent.getAttribute(Attributes.FOLLOW_RANGE);
    if (range != null) range.setBaseValue(160D);
    if (health != null) health.setBaseValue(health.getBaseValue() * healthScale);
    if (damage != null) damage.setBaseValue(damage.getBaseValue() * damageScale);
    if (speed != null) speed.setBaseValue(speed.getBaseValue() * 1.05);
    if (armor != null) armor.setBaseValue(armor.getBaseValue() * 1.10);
  }

  private void prepareEntityCreature(PathfinderMob creature, ServerPlayer player) {
    if (Configuration.COMMON.debug.get()) {
      creature.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000, 0));
      creature.setCustomName(Component.literal(this.toString()));
      creature.setCustomNameVisible(true);
    }
    creature.addTag("invasions_type_" + type.name().toLowerCase());
  }
}
