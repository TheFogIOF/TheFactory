package tfmg.main.items;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.payloads.Constructor;
import mindustry.world.meta.BuildVisibility;

import static tfmg.main.registry.Items.researchPlacehodler;

public class ResearchPlaceholderItem {
    public static void load() {
        researchPlacehodler = new Item("research-placeholder", Color.acid);
        researchPlacehodler.hardness = 1;
        researchPlacehodler.alwaysUnlocked = true;
    }
}
