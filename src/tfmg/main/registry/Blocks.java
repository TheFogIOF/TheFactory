package tfmg.main.registry;

import mindustry.world.blocks.payloads.Constructor;
import tfmg.main.blocks.ConstructorBlock;
import tfmg.main.blocks.CrafterBlock;
import tfmg.main.blocks.TerrainBlocks;
import tfmg.multicrafter.MultiCrafter;

public class Blocks {
    public static MultiCrafter crafterBlock;
    public static Constructor constructorBlock;

    public static void load() {
        TerrainBlocks.load();
        CrafterBlock.load();
        ConstructorBlock.load();
    }
}
