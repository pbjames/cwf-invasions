package cwf.dj;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class ManagerDataStore extends WorldSavedData {
  public static final String NAME = "invasion_worldstate";
  public static final Logger LOGGER = CWFInvasions.logger;

  @SubscribeEvent
  public static void onWorldLoad(WorldEvent.Load event) {
    World world = event.getWorld();
    if (!world.isRemote && world.provider.getDimension() == 0) {
      if (ManagerDataStore.isStored(world)) {
        LOGGER.info("Found save data‼");
        CWFInvasionsManager.loadFromWorld(world);
      } else {
        LOGGER.info("No save data, making it‼");
        CWFInvasionsManager.createAndStoreData(world);
      }
      CWFInvasionsManager.logConfig();
    }
  }

  public static ManagerDataStore retrieveData(World worldIn) {
    MapStorage storage = worldIn.getMapStorage();
    ManagerDataStore data = (ManagerDataStore) storage.getOrLoadData(ManagerDataStore.class, NAME);
    return data;
  }

  private static boolean isStored(World worldIn) {
    MapStorage storage = worldIn.getMapStorage();
    return storage.getOrLoadData(ManagerDataStore.class, NAME) != null;
  }

  public boolean invasionHappeningNow;
  public long timeAtStart;
  public int slainSinceStart;
  public String configName;

  public ManagerDataStore(
      boolean invasionHappeningNow, long timeAtStart, int slainSinceStart, String configName) {
    super(NAME);
    this.invasionHappeningNow = invasionHappeningNow;
    this.timeAtStart = timeAtStart;
    this.slainSinceStart = slainSinceStart;
    this.configName = configName;
  }

  public ManagerDataStore() {
    super(NAME);
    this.invasionHappeningNow = false;
    this.timeAtStart = 0;
    this.slainSinceStart = 0;
    this.configName = "template";
  }

  public ManagerDataStore(String name) {
    super(name);
    this.invasionHappeningNow = false;
    this.timeAtStart = 0;
    this.slainSinceStart = 0;
    this.configName = "template";
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    this.invasionHappeningNow = nbt.getBoolean("invasionHappeningNow");
    this.timeAtStart = nbt.getLong("timeAtStart");
    this.slainSinceStart = nbt.getInteger("slainSinceStart");
    this.configName = nbt.getString("configName");
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setBoolean("invasionHappeningNow", invasionHappeningNow);
    compound.setLong("timeAtStart", timeAtStart);
    compound.setInteger("slainSinceStart", slainSinceStart);
    compound.setString("configName", configName == null ? "template" : configName);
    return compound;
  }
}
