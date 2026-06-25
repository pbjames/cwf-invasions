package cwf.dj.invasions.integrations;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class EAICompat {
  public static final boolean LOADED = ModList.get().isLoaded("enhancedai");

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
    if (!LOADED || event.getLevel().isClientSide) return;
    if (!(event.getEntity() instanceof PathfinderMob creature)) return;
    if (!creature.getTags().contains("invasions_type_miner")) return;
    CompoundTag forgeData = creature.getPersistentData();
    CompoundTag eaiTag = forgeData.getCompound("enhancedai");
    CompoundTag minerTag = eaiTag.getCompound("miner_mobs");
    minerTag.putBoolean("miner", true);
    eaiTag.put("miner_mobs", minerTag);
    forgeData.put("enhancedai", eaiTag);
  }
}
