package cwf.dj.invasions.invasion_config;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class InvasionConfigCollection {
  public static Map<String, InvasionConfig> configs = new HashMap<>();

  public static void loadFrom(Path configDirectory) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory)) {
      for (Path entry : stream) {
        if (entry.endsWith("template.toml")) continue;
        CommentedFileConfig fileConfig = CommentedFileConfig.of(entry);
        fileConfig.load();
        InvasionConfig config = new ObjectConverter().toObject(fileConfig, InvasionConfig::new);
        fileConfig.close();
        configs.put(entry.getFileName().toString(), config);
      }
    }
  }

  public static void writeTemplate(Path configDirectory) throws IOException {
    File configFile = configDirectory.resolve("template.toml").toFile();
    InvasionConfig myConfig = new InvasionConfig();
    CommentedFileConfig fileConfig = CommentedFileConfig.of(configFile);
    new ObjectConverter().toConfig(myConfig, fileConfig);
    fileConfig.setComment(
        "maintainedPopulation", "Upper limit of how many active invasion mobs there can be");
    fileConfig.setComment(
        "healthScalingWeight",
        "Weight multiplier for mob health scaling (relative to other attributes)");
    fileConfig.setComment(
        "damageScalingWeight",
        "Weight multiplier for mob damage scaling (relative to other attributes)");
    fileConfig.setComment(
        "totalMobScalingFactor", "Overall scaling factor applied across all weighted attributes");
    fileConfig.setComment(
        "startCondition.type",
        "How the invasion starts (TIME_OF_DAY, EVERY_N_DAYS, GAMESTAGE, DIMENSION, NEVER)");
    fileConfig.setComment(
        "endingCondition.type",
        "How the invasion ends (MOBS_KILLED, TIME_OF_DAY, AFTER_N_DAYS, GAMESTAGE, NEVER)");
    fileConfig.save();
    fileConfig.close();
  }
}
