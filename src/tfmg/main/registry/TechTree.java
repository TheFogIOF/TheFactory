package tfmg.main.registry;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.Objectives;
import mindustry.gen.Sounds;
import mindustry.type.ItemStack;
import tfmg.main.items.ResearchPlaceholderItem;
import tfmg.main.research.CustomResearch;
import tfmg.main.blocks.ConstructorBlock;
import tfmg.main.blocks.CrafterBlock;
import tfmg.main.registry.Blocks;

import static mindustry.content.TechTree.*;

public class TechTree {
    public static void load(){
        TechNode root = nodeRoot("The Factory", Planets.erida, () -> {});

        TechNode crafter_block = new TechNode(root, Blocks.crafterBlock, Blocks.crafterBlock.researchCost);

        TechNode arc_block = new TechNode(crafter_block, mindustry.content.Blocks.arc, Blocks.constructorBlock.researchCost);

        TechNode constructor_block = new TechNode(crafter_block, Blocks.constructorBlock, Blocks.constructorBlock.researchCost);
        constructor_block.objectives = Seq.with(new CustomResearch(Blocks.crafterBlock));

        TechNode battery = new TechNode(root, mindustry.content.Blocks.battery, Blocks.constructorBlock.researchCost);

        TechNode mechanicalDrill = new TechNode(battery, mindustry.content.Blocks.laserDrill, ItemStack.with(Items.researchPlacehodler, 1));
        mechanicalDrill.objectives = Seq.with(new CustomResearch(mindustry.content.Blocks.battery));

        root.planet = Planets.erida;
        root.children.each(c -> c.planet = Planets.erida);
    }
}
