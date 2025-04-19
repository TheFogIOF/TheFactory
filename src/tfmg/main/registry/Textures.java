package tfmg.main.registry;

import arc.Core;
import arc.graphics.g2d.NinePatch;
import arc.graphics.g2d.TextureAtlas;
import arc.scene.style.Drawable;
import arc.scene.style.ScaledNinePatchDrawable;
import arc.scene.style.TextureRegionDrawable;

public class Textures {
    public static Drawable buttonParentResearch;

    public static void load() {
        buttonParentResearch = Core.atlas.drawable("thefactory-button-parent-research");
    }
}
