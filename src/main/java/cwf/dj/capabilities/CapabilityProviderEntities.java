package cwf.dj.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import cwf.dj.CWFInvasions;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityProviderEntities implements ICapabilitySerializable<NBTTagCompound> {

  private static final String STAR_LEVEL_NBT = "star";
  private StarLevel starLevel = new StarLevel();

  private static final Logger logger = CWFInvasions.logger;

  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return true;
  }

  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    if (capability == CapabilityStarLevel.CAPABILITY_STAR_LEVEL) {
      logger.info("StarLevel capability has been provided");
      return (T) this.starLevel;
    }
    return null;
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound nbt = new NBTTagCompound();
    NBTTagInt starLevelTag = (NBTTagInt) CapabilityStarLevel.CAPABILITY_STAR_LEVEL.writeNBT(starLevel, EnumFacing.UP);
    nbt.setInteger(STAR_LEVEL_NBT, starLevelTag.getInt());
    return nbt;
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    NBTBase starLevelTag = nbt.getTag(STAR_LEVEL_NBT);
    CapabilityStarLevel.CAPABILITY_STAR_LEVEL.readNBT(starLevel, EnumFacing.UP, starLevelTag);
  }
}
