package com.example;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = CFWInvasions.MODID, name = CFWInvasions.NAME, version = CFWInvasions.VERSION)
public class CFWInvasions {
  public static final String MODID = "invasions";
  public static final String NAME = "CFW Invasions";
  public static final String VERSION = "1.0";

  public static final Logger LOGGER = LogManager.getLogger(MODID);

  @Mod.EventHandler
  public void preinit(FMLPreInitializationEvent preinit) {
    LOGGER.info("CFW Invasions pre-init!");
  }
}
