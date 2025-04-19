package tfmg.main.core.floor;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.content.Blocks;
import mindustry.graphics.CacheLayer;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.Arrays;

public class CustomFloor extends Floor {

    public CustomFloor(String name, int variants) {
        super(name, variants);
    }

    @Override
    protected void drawEdges(Tile tile) {
        this.blenders.clear();
        this.blended.clear();
        Arrays.fill(this.dirs, 0);
        CacheLayer realCache = tile.floor().cacheLayer;
        for(int i = 0; i < 8; ++i) {
            Point2 point = Geometry.d8[i];
            Tile other = tile.nearby(point);

            if (other != null && !other.floor().name.contains(tile.floor().name.replaceAll("[0-9]","")) && this.doEdge(tile, other, other.floor()) && other.floor() != Blocks.empty) {
                if (!this.blended.getAndSet(other.floor().id)) {
                    blenders.add(other.floor());
                }
                this.dirs[i] = other.floorID();
            }
        }

        this.drawBlended(tile, true);
    }
}
