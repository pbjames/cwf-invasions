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
    // @RequiresMcRestart
    @Name("Enable Logging")
    @Comment("Turns on logging information from the mod")
    public boolean enableDebugLogging = true;
  }

  public static class Common {
    @Name("Enable Hardcore Mode")
    @Comment("If true, activates HARDCORE MODE")
    public boolean hardcoreMode = false;

    @Name("Slow Tick Checking")
    @Comment("When to check for invasion conditions in ticks")
    @RangeInt(min = 1)
    public int slowTickTime = 10 * 20;

    @Name("Fast Tick Checking")
    @Comment("How often we refresh mobs in the invasion in ticks")
    @RangeInt(min = 1)
    public int fastTickTime = 5 * 20;

    @Name("Mob Maintenance Level")
    @Comment("Higher value = more mobs during the invasion")
    public int mobMaintainCount = 20;

    @Name("Maximum Spawn Distance from Player")
    @Comment({
      "Block-distance mobs are allowed to spawn away from the player",
      "Note: Mobs will spawn as close as they can currently"
    })
    public int maxInvadeDistance = 32;

    @Name("Minimum Spawn Distance from Player")
    @Comment("Disallow spawning mobs within N blocks from player")
    public int minInvadeDistance = 5;

    @Name("Show Particle Effects")
    @Comment("Whether the client renders spicy particles.")
    public boolean showParticles = true;
  }
}
