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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import org.yaml.snakeyaml.Yaml;

public class InvasionsConfiguration {
  public static List<InvasionConfig> configs = new ArrayList<>();

  public static void loadFrom(Path configDirectory) throws IOException {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDirectory)) {
      for (Path entry : stream) {
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
  public InvadeCondition condition = InvadeCondition.HOLDINGCHICKEN;
  public InvadeMobClass[] mobClasses = {new InvadeMobClass(EntityList.getKey(EntityZombie.class))};
  public int healthScalingWeight = 1;
  public int maintainedPopulation = 5;
  public int damageScalingWeight = 1;
}

enum InvadeCondition {
  GAMESTAGE,
  HOLDINGCHICKEN
}

enum InvadeThresh {
  MOBCOUNT,
  TIME,
}

class InvadeMobClass {
  public ResourceLocation ent;
  public InvadeMobType type;

  public InvadeMobClass(ResourceLocation ent, InvadeMobType type) {
    this.ent = ent;
    this.type = type;
  }

  public InvadeMobClass(ResourceLocation ent) {
    this.ent = ent;
    this.type = InvadeMobType.CQC;
  }
}

enum InvadeMobType {
  CQC(0),
  GUNNER(1),
  MINER(2),
  SUPPORT(3);

  InvadeMobType(int aiTaskKind) {}
}
