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
    fileConfig.save();
    fileConfig.close();
  }
}
