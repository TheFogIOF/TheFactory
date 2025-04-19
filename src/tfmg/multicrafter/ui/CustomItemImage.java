package tfmg.multicrafter.ui;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Styles;

public class CustomItemImage extends Stack {
    public CustomItemImage(TextureRegion region, int amount) {
        add(new Table((o) -> {
            o.left();
            o.add(new Image(region)).size(32.0F).scaling(Scaling.fit);
        }));
        if (amount != 0) {
            add(new Table((t) -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount((long)amount) : amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }

    }

    public CustomItemImage(ItemStack stack) {
        this(stack.item.uiIcon, stack.amount);
    }

    public CustomItemImage(PayloadStack stack) {
        this(stack.item.uiIcon, stack.amount);
    }
}
