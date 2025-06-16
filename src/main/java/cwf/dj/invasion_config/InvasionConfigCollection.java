package cwf.dj.invasion_config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;

public class InvasionConfigCollection {
  public static List<InvasionConfig> configs = new ArrayList<>();

  public static void loadFrom(Path configDirectory) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory)) {
      for (Path entry : stream) {
        if (entry.endsWith("template.yml")) continue;
        FileInputStream iStream = new FileInputStream(entry.toFile());
        Yaml yaml = new Yaml();
        InvasionConfig config = yaml.loadAs(iStream, InvasionConfig.class);
        configs.add(config);
        iStream.close();
      }
    }
  }

  public static void writeTemplate(Path configDirectory) throws IOException {
    File configFile = configDirectory.resolve("template.yml").toFile();
    Yaml yaml = new Yaml();
    try (FileWriter writer = new FileWriter(configFile)) {
      yaml.dump(new InvasionConfig(), writer);
    }
  }
}

