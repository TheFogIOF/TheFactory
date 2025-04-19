package tfmg.main.registry;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.graphics.g3d.NoiseMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Item;
import mindustry.type.Planet;
import mindustry.world.Block;

public class Planets {
    public static Planet erida;
    public static void load() {
        int randomSeed = (int) (System.currentTimeMillis() * System.currentTimeMillis() / System.currentTimeMillis());

        erida = new Planet("erida", mindustry.content.Planets.sun,1F,1);
        erida.generator = new SerpuloPlanetGenerator();
        erida.meshLoader = () -> new HexMesh(erida, 6);
        erida.cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(erida, 11, 0.15f, 0.13f, 5, Color.orange.cpy().set(Pal.spore).mul(0.9f).a(0.75f), 2, 0.45f, 0.9f, 0.38f),
                new HexSkyMesh(erida, 1, 0.6f, 0.16f, 5, Color.coral.cpy().lerp(Pal.spore, 0.55f).a(0.75f), 2, 0.45f, 1f, 0.41f)
        );
        //erida.meshLoader = () -> new NoiseMesh(erida, 485312, 5, Color.valueOf("d47420"), 0.8F, 7, 1, 1, 1.2F);
        erida.launchCapacityMultiplier = 0.5f;
        erida.allowLegacyLaunchPads = true;
        erida.alwaysUnlocked = true;
        erida.defaultCore = Blocks.coreAcropolis; // Привязано к ресурсам высадки
        erida.sectorSeed = 42 + randomSeed;
        erida.startSector = 1;
        erida.iconColor = Color.valueOf("ffcc00");
        erida.atmosphereColor = Color.coral;
    }
}
