package cwf.dj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.yaml.snakeyaml.Yaml;

public class InvasionsConfiguration {
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

class InvasionConfig {
  public InvadeStartCondition startCondition = InvadeStartCondition.NIGHT;
  public InvadeEndCondition endingCondition = InvadeEndCondition.MOBCOUNT;
  public int mobCountToEnd = 120;
  public int timeToEndTicks = 300 * 20;
  public int maintainedPopulation = 10;
  public int healthScalingWeight = 2;
  public int damageScalingWeight = 1;
  public InvadeMobClass[] mobClasses = {
    new InvadeMobClass("minecraft:zombie", 5, InvadeMobType.CQC),
    new InvadeMobClass("minecraft:skeleton", 2, InvadeMobType.SUPPORT)
  };

  public InvadeMobClass pickRandomMobClass() {
    int totalWeight = 0;
    for (InvadeMobClass mobClass : mobClasses) totalWeight += mobClass.weight;
    int selected = new Random().nextInt(totalWeight - 1);
    int runSum = 0;
    for (InvadeMobClass mobClass : mobClasses) {
      runSum += mobClass.weight;
      if ((selected <= runSum) && (selected >= (runSum - mobClass.weight))) return mobClass;
    }
    return mobClasses[mobClasses.length - 1];
  }
}

enum InvadeStartCondition {
  DAY,
  NIGHT,
  FORTNIGHT,
  FULL_MOON;
}

enum InvadeEndCondition {
  MOBCOUNT,
  TIME
}
