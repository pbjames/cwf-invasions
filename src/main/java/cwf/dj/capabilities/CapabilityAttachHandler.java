package cwf.dj.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityAttachHandler {

  @SubscribeEvent
  public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    if (entity instanceof EntityPlayer) {
      event.addCapability(
          new ResourceLocation("cwf:star_level_capability"),
          new CapabilityProviderEntities());
    }
  }
}
