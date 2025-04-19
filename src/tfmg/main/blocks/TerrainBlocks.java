package tfmg.main.blocks;

import mindustry.content.Items;
import mindustry.graphics.Layer;
import mindustry.world.blocks.environment.Prop;
import tfmg.main.core.floor.CustomFloor;
import tfmg.main.core.floor.GenerateFloor;
import tfmg.main.core.floor.TileFloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerrainBlocks {
    public static CustomFloor grass;
    public static List<TileFloor> grass_medium = new ArrayList<>();
    public static List<TileFloor> grass_large = new ArrayList<>();
    public static GenerateFloor grass_generate;

    public static CustomFloor dirt;
    public static List<TileFloor> dirt_medium = new ArrayList<>();
    public static List<TileFloor> dirt_large = new ArrayList<>();
    public static GenerateFloor dirt_generate;

    public static CustomFloor sand;
    public static List<TileFloor> sand_medium = new ArrayList<>();
    public static List<TileFloor> sand_large = new ArrayList<>();
    public static GenerateFloor sand_generate;

    public static Prop grass_prop;

    public static void load() {
        loadGrass();
        loadDirt();
        loadSand();
        grass_prop = new Prop("hr-green-small-grass");
        grass_prop.variants = 12;
        grass_prop.hasShadow = false;
        grass_prop.breakable = false;
        grass_prop.layer = Layer.floor + 1;
    }

    public static void loadGrass() {
        grass = new CustomFloor("grass",16);
        for (int i = 1; i <= 256; i++) grass_large.add(new TileFloor("grass-large" + i, 4));
        for (int i = 1; i <= 64; i++) grass_medium.add(new TileFloor("grass-medium" + i, 2));
        grass_generate = new GenerateFloor("grass-generate",16, grass, Arrays.asList(grass_large, grass_medium));
    }

    public static void loadDirt() {
        dirt = new CustomFloor("dirt",16);
        for (int i = 1; i <= 256; i++) dirt_large.add(new TileFloor("dirt-large" + i, 4));
        for (int i = 1; i <= 64; i++) dirt_medium.add(new TileFloor("dirt-medium" + i, 2));
        dirt_generate = new GenerateFloor("dirt-generate",16, dirt, Arrays.asList(dirt_large, dirt_medium));
    }

    public static void loadSand() {
        sand = new CustomFloor("sand",16);
        sand.localizedName = "@thefactory-sand";
        sand.itemDrop = Items.sand;
        sand.playerUnmineable = true;
        for (int i = 1; i <= 256; i++) {
            TileFloor tileFloor = new TileFloor("sand-large" + i, 4);
            tileFloor.localizedName = "@thefactory-sand";
            tileFloor.itemDrop = Items.sand;
            tileFloor.playerUnmineable = true;
            sand_large.add(tileFloor);
        }
        //for (int i = 1; i <= 64; i++) sand_medium.add(new TileFloor("sand-medium" + i, 2));
        sand_generate = new GenerateFloor("sand-generate",16, sand, Arrays.asList(sand_large));
    }
}
