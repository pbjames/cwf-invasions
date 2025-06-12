package cwf.dj.proxy;

import cwf.dj.capabilities.CapabilityStarLevel;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
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
}
