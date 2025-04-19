package tfmg.main;

import arc.*;
import arc.scene.ui.layout.Table;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.Styles;
import tfmg.main.registry.*;
import tfmg.main.research.CustomResearchDialog;

public class TheFactoryMod extends Mod{

    public TheFactoryMod(){
        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                Log.info("Client started");
                Textures.load();
            });
        });
        Events.on(WorldLoadEvent.class, e -> {;
            Time.runTask(10f, () -> {
                Log.info("Custom TechTree loaded");
                customTechTree();
            });
        });
    }

    public void customTechTree() {
        CustomResearchDialog researchDialog = new CustomResearchDialog();
        Table button = new Table();
        if (!Vars.mobile) button.setPosition(24 + 8,24 + 8); else button.setPosition(24 + 155,24);
        button.button(Icon.tree, Styles.cleari, 48, researchDialog::show).visible(() -> Vars.state.isCampaign()).tooltip("@research");
        Vars.ui.hudGroup.addChild(button);
    }

    @Override
    public void loadContent(){
        Log.info("Loading content.");
        Blocks.load();
        Items.load();
        Planets.load();
        Sectors.load();
        TechTree.load();
    }
}
