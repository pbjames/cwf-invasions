package cwf.dj;


import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = CWFInvasions.MODID)
public class Configuration {
  public static Client client = new Client();
  public static Common common = new Common();

  public static class Client {
  }

  public static class Common {
    @Name("Slow Tick Checking")
    @Comment("When to check for invasion conditions in ticks")
    @RangeInt(min = 1)
    public int slowTickTime = 10 * 20;

    @Name("Fast Tick Checking")
    @Comment("How often we refresh mobs in the invasion in ticks")
    @RangeInt(min = 1)
    public int fastTickTime = 5 * 20;
  }
}
