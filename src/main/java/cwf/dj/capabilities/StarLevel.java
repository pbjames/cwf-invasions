package cwf.dj.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StarLevel {
  private int level;

  public StarLevel() {
    this(0);
  }

  public StarLevel(int initialLevel) {
    level = initialLevel;
  }

  public void addLevel(int levelDelta) {
    level += levelDelta;
  }

  public int getLevel() {
    return level;
  }

  public static StarLevel newInstance() {
    return new StarLevel();
  }

  public static class StarLevelNBTStorage implements Capability.IStorage<StarLevel> {

    @Override
    public NBTBase writeNBT(Capability<StarLevel> capability, StarLevel instance, EnumFacing side) {
      NBTTagInt intNBT = new NBTTagInt(instance.level);
      return intNBT;
    }

    @Override
    public void readNBT(
        Capability<StarLevel> capability, StarLevel instance, EnumFacing side, NBTBase nbt) {
      int tagLevel = 0;
      if (nbt instanceof NBTTagInt) {
        tagLevel = ((NBTTagInt) nbt).getInt();
      }
      instance.addLevel(tagLevel);
    }
  }
}
