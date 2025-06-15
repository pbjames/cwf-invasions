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
  public InvadeCondition startCondition = InvadeCondition.HOLDINGCHICKEN;
  public InvadeThresh endingCondition = InvadeThresh.MOBCOUNT_XS;
  public int maintainedPopulation = 10;
  public int healthScalingWeight = 2;
  public int damageScalingWeight = 1;
  public InvadeMobClass[] mobClasses = {
    new InvadeMobClass("minecraft:zombie", 5),
    new InvadeMobClass("minecraft:skeleton", 2, InvadeMobType.SUPPORT)
  };
}

enum InvadeCondition {
  // TODO: Flesh these out
  GAMESTAGE,
  HOLDINGCHICKEN
}

enum InvadeThresh {
  MOBCOUNT_HIGH(500),
  MOBCOUNT_MEDIUM(200),
  MOBCOUNT_LOW(70),
  MOBCOUNT_XS(40),
  TIME_60M(4800 * 20),
  TIME_30M(2400 * 20),
  TIME_20M(1200 * 20),
  TIME_10M(600 * 20),
  TIME_5M(300 * 20);

  public final int value;

  InvadeThresh(int value) {
    this.value = value;
  }
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

  public InvadeMobClass(String ent, int weight) {
    this.ent = ent;
    this.weight = weight;
    this.type = InvadeMobType.CQC;
  }

  public InvadeMobClass(String ent) {
    this.ent = ent;
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
