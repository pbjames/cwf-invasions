package cwf.dj;

import cwf.dj.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = CWFInvasions.MODID, name = CWFInvasions.NAME, version = CWFInvasions.VERSION)
public class CWFInvasions {
  public static final String MODID = "invasions";
  public static final String NAME = "CFW Invasions";
  public static final String VERSION = "1.0";
  public static Logger logger;

  @SidedProxy(clientSide = "cwf.dj.proxy.ClientProxy", serverSide = "cwf.dj.proxy.ServerProxy")
  public static CommonProxy proxy;

  @Mod.Instance public static CWFInvasions instance;

  @Mod.EventHandler
  public void preinit(FMLPreInitializationEvent preinit) {
    logger = preinit.getModLog();
    logger.info("CFW Invasions pre-init!");
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
    proxy.preInit(event);
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent e) {
    proxy.init(e);
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent e) {
    proxy.postInit(e);
  }
}
