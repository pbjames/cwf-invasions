package cwf.dj.invasions.integrations;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class GameStagesCompat {
  public static final boolean LOADED = ModList.get().isLoaded("gamestages");

  public static class Impl {
    public static void addStage(ServerPlayer player, String stage) {
      GameStageHelper.addStage(player, stage);
    }

    public static boolean hasStage(Player player, String stage) {
      return GameStageHelper.hasStage(player, stage);
    }
  }

  public static void addStage(ServerPlayer player, String stage) {
    if (!LOADED) return;
    Impl.addStage(player, stage);
  }

  public static boolean hasStage(Player player, String stage) {
    if (!LOADED) return false;
    return Impl.hasStage(player, stage);
  }
}
