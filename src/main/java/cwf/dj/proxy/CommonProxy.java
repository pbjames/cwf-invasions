package cwf.dj.proxy;

import cwf.dj.CWFInvasions;
import cwf.dj.capabilities.CapabilityStarLevel;
import cwf.dj.invasion_config.InvasionConfigCollection;
import java.io.IOException;
import java.nio.file.Path;
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
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CommonProxy {
  private static Path modConfigDir;

  public void preInit(FMLPreInitializationEvent e) {
    CapabilityStarLevel.register();
    modConfigDir = e.getModConfigurationDirectory().toPath();
    modConfigDir = modConfigDir.resolve("invasions");
    if (!modConfigDir.toFile().exists()) modConfigDir.toFile().mkdirs();
    try {
      InvasionConfigCollection.loadFrom(modConfigDir);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  public void init(FMLInitializationEvent e) {}

  public void postInit(FMLPostInitializationEvent e) {}

  public void onServerStopping(FMLServerStoppingEvent e) {
    try {
      InvasionConfigCollection.writeTemplate(modConfigDir);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  @SubscribeEvent
  public static void registerBlocks(RegistryEvent.Register<Block> event) {}

  @SubscribeEvent
  public static void registerItems(RegistryEvent.Register<Item> event) {}

  @SubscribeEvent
  public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
    CWFInvasions.logger.info("config reload");
    if (event.getModID().equals(CWFInvasions.MODID)) {
      CWFInvasions.logger.info("twas our mod");
      ConfigManager.sync(CWFInvasions.MODID, Config.Type.INSTANCE);
    }
  }
}
