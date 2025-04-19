package tfmg.main.research;

import arc.Core;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;

public class CustomResearch implements Objectives.Objective {
    public UnlockableContent content;

    public CustomResearch(UnlockableContent content) {
        this.content = content;
    }

    public boolean complete() {
        return content.unlocked();
    }

    public String display() {
        return Core.bundle.format("requirement.research", new Object[]{content.emoji() + "[red] " + content.localizedName + " "});
    }
}