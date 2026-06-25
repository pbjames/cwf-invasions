package cwf.dj.invasions;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configuration {
  public static final ForgeConfigSpec COMMON_SPEC;
  public static final Common COMMON;
  public static final ForgeConfigSpec CLIENT_SPEC;
  public static final Client CLIENT;

  static {
    ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
    COMMON = new Common(commonBuilder);
    COMMON_SPEC = commonBuilder.build();

    ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
    CLIENT = new Client(clientBuilder);
    CLIENT_SPEC = clientBuilder.build();
  }

  public static class Common {
    public final ForgeConfigSpec.IntValue slowTickTime;
    public final ForgeConfigSpec.IntValue fastTickTime;
    public final ForgeConfigSpec.BooleanValue minersGlow;
    public final ForgeConfigSpec.BooleanValue debug;

    public Common(ForgeConfigSpec.Builder builder) {
      slowTickTime =
          builder
              .comment("When to check for invasion conditions in ticks")
              .defineInRange("slowTickTime", 10 * 20, 1, Integer.MAX_VALUE);

      fastTickTime =
          builder
              .comment("How often we refresh mobs in the invasion in ticks")
              .defineInRange("fastTickTime", 5 * 20, 1, Integer.MAX_VALUE);

      minersGlow =
          builder
              .comment("Make mining mobs glow to make them easier to spot")
              .define("minersGlow", true);
      debug =
          builder
              .comment("Enable debug mode - logs in the console and everyone glows")
              .define("debug", false);
    }
  }

  public static class Client {
    public Client(ForgeConfigSpec.Builder builder) {}
  }
}
