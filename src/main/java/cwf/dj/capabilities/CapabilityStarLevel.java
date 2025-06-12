package cwf.dj.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityStarLevel {
  @CapabilityInject(StarLevel.class)
  public static Capability<StarLevel> CAPABILITY_STAR_LEVEL = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(
        StarLevel.class, new StarLevel.StarLevelNBTStorage(), StarLevel::newInstance);
  }
}
