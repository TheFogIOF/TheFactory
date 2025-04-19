package tfmg.main.blocks;

import arc.struct.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import tfmg.multicrafter.*;

import static tfmg.main.registry.Blocks.crafterBlock;

public class CrafterBlock {
    public static void load() {
        crafterBlock = new MultiCrafter("crafter-lvl-1");
        crafterBlock.requirements = ItemStack.with(Items.copper, 1, Items.lead, 1);
        crafterBlock.buildVisibility = BuildVisibility.shown;
        crafterBlock.category = Category.crafting;
        crafterBlock.size = 3;
        crafterBlock.menu = "Detailed";
        crafterBlock.researchCost = ItemStack.with(
                Items.copper, 1,
                Items.lead, 1
        );
        crafterBlock.resolvedRecipes = Seq.with(
                new Recipe() {{
                    input = new IOEntry() {{
                        items = ItemStack.with(
                                Items.copper, 1,
                                Items.lead, 1
                        );
                        payloads = PayloadStack.with(
                                Blocks.thoriumWall, 2
                        );
                    }};
                    output = new IOEntry() {{
                        items = ItemStack.with(
                                Items.surgeAlloy, 1,
                                Items.thorium, 1
                        );
                    }};
                    craftTime = 120f;
                }},
                new Recipe() {{
                    input = new IOEntry() {{
                        items = ItemStack.with(
                                Items.plastanium, 1,
                                Items.pyratite, 1
                        );
                        fluids = LiquidStack.with(
                                Liquids.slag, 0.5f
                        );
                    }};
                    output = new IOEntry() {{
                        items = ItemStack.with(
                                Items.coal, 1,
                                Items.sand, 1
                        );
                        fluids = LiquidStack.with(
                                Liquids.oil, 0.2f
                        );
                        payloads = PayloadStack.with(
                                UnitTypes.dagger, 1
                        );
                    }};
                    craftTime = 150f;
                }}
        );
    }
}
