package tfmg.main.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.meta.BuildVisibility;

import static tfmg.main.registry.Blocks.constructorBlock;

public class ConstructorBlock {
    public static void load() {
        constructorBlock = new Constructor("block-menu");
        constructorBlock.buildVisibility = BuildVisibility.shown;
        constructorBlock.category = Category.crafting;
        constructorBlock.size = 3;
        constructorBlock.researchCost = ItemStack.with(
                Items.copper, 1,
                Items.lead, 1
        );
    }

    //drill
    /*
    consumeItem(Item item, int amount)
    hasItems

     */
}
