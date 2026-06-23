package cwf.dj.invasions;

import com.mojang.logging.LogUtils;
import cwf.dj.invasions.invasion_config.InvasionConfigCollection;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(Invasions.MODID)
public class Invasions {
  public static final String MODID = "invasions";
  public static final Logger LOGGER = LogUtils.getLogger();
  public static final Path MOD_CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("invasions");

  public Invasions(FMLJavaModLoadingContext context) {
    IEventBus modEventBus = context.getModEventBus();
    modEventBus.addListener(this::commonSetup);
    context.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_SPEC);
    context.registerConfig(ModConfig.Type.CLIENT, Configuration.CLIENT_SPEC);
    // Register ourselves for server and other game events we are interested in
    // MinecraftForge.EVENT_BUS.register(this);
    //
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    LOGGER.info("HELLO FROM COMMON SETUP");
    if (!MOD_CONFIG_DIR.toFile().exists()) MOD_CONFIG_DIR.toFile().mkdirs();
    try {
      InvasionConfigCollection.writeTemplate(MOD_CONFIG_DIR);
      InvasionConfigCollection.loadFrom(MOD_CONFIG_DIR);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {
    LOGGER.info("HELLO from server starting");
  }

  // You can use EventBusSubscriber to automatically register all static methods in the class
  // annotated with @SubscribeEvent
  @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      LOGGER.info("HELLO FROM CLIENT SETUP");
      LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
  }
}
