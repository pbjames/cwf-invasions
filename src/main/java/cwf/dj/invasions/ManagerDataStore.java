package cwf.dj.invasions;

import cwf.dj.invasions.invasion_config.InvasionConfigCollection;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Invasions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManagerDataStore extends SavedData {
  public static final String NAME = "invasion_worldstate";

  @SubscribeEvent
  public static void onServerLevelLoad(LevelEvent.Load event) {
    if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
    if (serverLevel.dimension() == Level.OVERWORLD) {
      InvasionsManager.loadServerLevelData(serverLevel);
      // CWFInvasionsManager.logSelf();
    }
    try {
      InvasionConfigCollection.loadFrom(Invasions.MOD_CONFIG_DIR);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean invasionHappeningNow;
  public long timeAtStart;
  public int slainSinceStart;
  public String configName;

  public ManagerDataStore(
      boolean invasionHappeningNow, long timeAtStart, int slainSinceStart, String configName) {
    this.invasionHappeningNow = invasionHappeningNow;
    this.timeAtStart = timeAtStart;
    this.slainSinceStart = slainSinceStart;
    this.configName = configName;
  }

  public ManagerDataStore() {
    this.invasionHappeningNow = false;
    this.timeAtStart = 0;
    this.slainSinceStart = 0;
    this.configName = "template";
  }

  public ManagerDataStore(String name) {
    this.invasionHappeningNow = false;
    this.timeAtStart = 0;
    this.slainSinceStart = 0;
    this.configName = "template";
  }

  public static ManagerDataStore load(CompoundTag tag) {
    ManagerDataStore data = new ManagerDataStore();
    data.invasionHappeningNow = tag.getBoolean("invasionHappeningNow");
    data.timeAtStart = tag.getInt("timeAtStart");
    data.slainSinceStart = tag.getInt("slainSinceStart");
    data.configName = tag.getString("configName");
    return data;
  }

  public static ManagerDataStore get(ServerLevel level) {
    return level
        .getDataStorage()
        .computeIfAbsent(ManagerDataStore::load, ManagerDataStore::new, NAME);
  }

  @Override
  public CompoundTag save(CompoundTag compound) {
    compound.putBoolean("invasionHappeningNow", invasionHappeningNow);
    compound.putLong("timeAtStart", timeAtStart);
    compound.putInt("slainSinceStart", slainSinceStart);
    compound.putString("configName", configName == null ? "template" : configName);
    return compound;
  }

  public void incrementSlain() {
    slainSinceStart += 1;
    setDirty();
  }

  public void setNewInvasion(long worldTime) {
    invasionHappeningNow = true;
    slainSinceStart = 0;
    timeAtStart = worldTime;
    setDirty();
  }

  public void setEndInvasion() {
    invasionHappeningNow = false;
    setDirty();
  }
}
