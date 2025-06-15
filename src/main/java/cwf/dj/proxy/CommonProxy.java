package cwf.dj.proxy;

import cwf.dj.CWFInvasions;
import cwf.dj.capabilities.CapabilityStarLevel;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CommonProxy {
  public void preInit(FMLPreInitializationEvent e) {
    CapabilityStarLevel.register();
  }

  public void init(FMLInitializationEvent e) {}

  public void postInit(FMLPostInitializationEvent e) {}

  @SubscribeEvent
  public static void registerBlocks(RegistryEvent.Register<Block> event) {}

  @SubscribeEvent
  public static void registerItems(RegistryEvent.Register<Item> event) {}

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
    if (event.getModID().equals(CWFInvasions.MODID)) {
      ConfigManager.sync(CWFInvasions.MODID, Config.Type.INSTANCE);
    }
  }
}
