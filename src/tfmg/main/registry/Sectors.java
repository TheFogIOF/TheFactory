package tfmg.main.registry;

import mindustry.maps.generators.BasicGenerator;
import mindustry.type.SectorPreset;

public class Sectors {
    public static SectorPreset erida_main,erida_high;
    public static void load() {
        erida_main = new SectorPreset("erida-main", Planets.erida, 1);
        erida_main.alwaysUnlocked = true;
        erida_main.showSectorLandInfo = true;
        erida_main.overrideLaunchDefaults = true;

        erida_high = new SectorPreset("erida-high", Planets.erida, 42);
        erida_high.alwaysUnlocked = true;
        erida_high.showSectorLandInfo = true;
    }
}
