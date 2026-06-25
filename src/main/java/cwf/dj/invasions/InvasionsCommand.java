package cwf.dj.invasions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import cwf.dj.invasions.invasion_config.InvasionConfig;
import cwf.dj.invasions.invasion_config.InvasionConfigCollection;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Invasions.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InvasionsCommand {

  @SubscribeEvent
  public static void onRegisterCommands(RegisterCommandsEvent event) {
    var dispatcher = event.getDispatcher();
    dispatcher.register(
        Commands.literal("invasion")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("status").executes(InvasionsCommand::status))
            .then(Commands.literal("reload").executes(InvasionsCommand::reload))
            .then(
                Commands.literal("start")
                    .then(
                        Commands.argument("config", StringArgumentType.word())
                            .suggests(InvasionsCommand::suggestConfigs)
                            .executes(InvasionsCommand::start)))
            .then(Commands.literal("end").executes(InvasionsCommand::end)));
  }

  private static int status(CommandContext<CommandSourceStack> ctx) {
    CommandSourceStack source = ctx.getSource();
    ManagerDataStore data = InvasionsManager.getData();
    if (InvasionsManager.level == null || data == null) {
      source.sendFailure(Component.literal("Invasion system not initialized"));
      return 0;
    }
    if (data.invasionHappeningNow) {
      long elapsed = (InvasionsManager.level.getGameTime() - data.timeAtStart);
      source.sendSuccess(
          () ->
              Component.literal(
                  "§cInvasion ACTIVE  §r| Config: §e"
                      + data.configName
                      + "  §r| Elapsed: §e"
                      + elapsed
                      + "s  §r| Slain: §e"
                      + data.slainSinceStart
                      + "  §r| Players: §e"
                      + InvasionsManager.players.size()),
          false);
    } else {
      source.sendSuccess(
          () ->
              Component.literal(
                  "No invasion active  | Loaded configs: §e"
                      + InvasionConfigCollection.configs.keySet()),
          false);
    }
    return 1;
  }

  private static int reload(CommandContext<CommandSourceStack> ctx) {
    CommandSourceStack source = ctx.getSource();
    try {
      InvasionConfigCollection.configs.clear();
      InvasionConfigCollection.loadFrom(Invasions.MOD_CONFIG_DIR);
      source.sendSuccess(
          () ->
              Component.literal(
                  "§aReloaded §e"
                      + InvasionConfigCollection.configs.size()
                      + "§a invasion configs: §e"
                      + InvasionConfigCollection.configs.keySet()),
          true);
    } catch (IOException e) {
      source.sendFailure(Component.literal("§cFailed to reload configs: " + e.getMessage()));
      Invasions.LOGGER.error("Failed to reload invasion configs", e);
      return 0;
    }
    return InvasionConfigCollection.configs.size();
  }

  private static CompletableFuture<Suggestions> suggestConfigs(
      CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
    InvasionConfigCollection.configs.keySet().forEach(builder::suggest);
    return builder.buildFuture();
  }

  private static int start(CommandContext<CommandSourceStack> ctx) {
    CommandSourceStack source = ctx.getSource();
    String configName = StringArgumentType.getString(ctx, "config");
    ManagerDataStore data = InvasionsManager.getData();
    if (InvasionsManager.level == null || data == null) {
      source.sendFailure(Component.literal("Invasion system not initialized"));
      return 0;
    }
    InvasionConfig config = InvasionConfigCollection.configs.get(configName);
    if (config == null) {
      source.sendFailure(
          Component.literal(
              "§cUnknown config: §e"
                  + configName
                  + "§c. Available: §e"
                  + InvasionConfigCollection.configs.keySet()));
      return 0;
    }
    if (data.invasionHappeningNow) {
      source.sendFailure(
          Component.literal("§cAn invasion is already active. Use §e/invasion end§c first."));
      return 0;
    }
    data.configName = configName;
    data.cooldownTimeStamp = InvasionsManager.level.getGameTime();
    InvasionsManager.startInvasion(config);
    data.setDirty();
    source.sendSuccess(() -> Component.literal("§aStarted invasion: §e" + configName), true);
    return 1;
  }

  private static int end(CommandContext<CommandSourceStack> ctx) {
    CommandSourceStack source = ctx.getSource();
    ManagerDataStore data = InvasionsManager.getData();
    if (InvasionsManager.level == null || data == null) {
      source.sendFailure(Component.literal("Invasion system not initialized"));
      return 0;
    }
    if (!data.invasionHappeningNow) {
      source.sendFailure(Component.literal("§cNo invasion is currently active."));
      return 0;
    }
    String configName = data.configName;
    InvasionConfig config = InvasionConfigCollection.configs.get(configName);
    InvasionsManager.endInvasion(config != null ? config : new InvasionConfig());
    source.sendSuccess(() -> Component.literal("§aEnded invasion: §e" + configName), true);
    return 1;
  }
}
