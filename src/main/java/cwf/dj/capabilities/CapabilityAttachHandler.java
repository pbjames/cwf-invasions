package cwf.dj.capabilities;

import org.apache.logging.log4j.Logger;

import cwf.dj.CWFInvasions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CapabilityAttachHandler {
  public static final Logger logger = CWFInvasions.logger;

  @SubscribeEvent
  public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    logger.debug("Capabilities attached");
    if (entity instanceof EntityPlayer) {
      event.addCapability(
          new ResourceLocation("cwf:star_level_capability"),
          new CapabilityProviderEntities());
    }
  }
}
