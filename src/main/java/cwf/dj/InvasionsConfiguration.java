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

class InvadeMobClass {
  public final String ent;
  public final int weight;
  public final InvadeMobType type;

  public InvadeMobClass(String ent, int weight, InvadeMobType type) {
    this.ent = ent;
    this.weight = weight;
    this.type = type;
  }
  public InvadeMobClass() {
    this.ent = "default";
    this.weight = 1;
    this.type = InvadeMobType.CQC;
  }
}

enum InvadeMobType {
  // TODO: Leaving this for later
  CQC(0),
  GUNNER(1),
  MINER(2),
  SUPPORT(3);

  InvadeMobType(int aiTaskKind) {}
}
