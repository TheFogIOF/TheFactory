package tfmg.main.core.floor;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.*;

public class GenerateFloor extends Floor {

    private Floor smallFloor;
    private List<List<TileFloor>> floorTilesList;

    public GenerateFloor(String name, int variants, Floor smallFloor, List<List<TileFloor>> floorTilesList) {
        super(name, variants);
        this.smallFloor = smallFloor;
        this.floorTilesList = floorTilesList;
    }

    public Floor getFloorNear(Tile tile, int x, int y) {
        if (tile.nearby(x,y) != null) return tile.nearby(x,y).floor();
        return tile.floor();
    }

    public boolean tileIsNotClose(Tile tile, int size) {
        for (int i = -1; i < size + 1; i++) {
            for (List<TileFloor> floorTiles : floorTilesList) {
                String tileSize = "";
                if (floorTiles.get(0).name.split("-").length > 1) tileSize = floorTiles.get(0).name.replaceAll("[0-9]","").split("-")[2];
                if (getFloorNear(tile, i, size + 1).name.contains(tileSize) || getFloorNear(tile, i, -1).name.contains(tileSize)) return false;
                if (getFloorNear(tile, -1, i).name.contains(tileSize) || getFloorNear(tile, size + 1, i).name.contains(tileSize)) return false;
            }
        }
        return true;
    }

    public Integer cordsToInt(String input, int gridSize) {
        Map<String, Integer> transformationMap = createGridSizeMap(gridSize);
        if (transformationMap == null) return 1;
        return transformationMap.get(input);
    }

    public Map<String, Integer> createGridSizeMap(int gridSize) {
        if (gridSize <= 0) return null;

        Map<String, Integer> transformationMap = new HashMap<>();
        int counter = 1;

        for (int x = gridSize - 1; x >= 0; x--) {
            for (int y = 0; y < gridSize; y++) {
                String key = String.format("%01d%01d", y, x);
                transformationMap.put(key, counter);
                counter++;
            }
        }

        return transformationMap;
    }

    public Floor getTextureFromCords(String cords, int variant, List<TileFloor> floorTile, int size) {
        return floorTile.get((cordsToInt(cords,size) - 1) + (size*size) * variant);
    }

    @Override
    public void drawBase(Tile tile) {
        Mathf.rand.setSeed((long)tile.pos());
        Draw.rect(this.variantRegions[this.variant(tile.x, tile.y)], tile.worldx(), tile.worldy());

        for (List<TileFloor> floorTiles : floorTilesList) {
            int tileSize = floorTiles.get(0).tileSize;

            Boolean floorIsLarge = true;
            for (int x = 0; x < tileSize; x++) for (int y = 0; y < tileSize; y++)
                if (tile.nearby(x,y) != null && tile.nearby(x, y).floor() != tile.floor())
                    floorIsLarge = false;

            if (floorIsLarge && Mathf.random(0,10) == 5 && tileIsNotClose(tile, tileSize)) {
                int random = Mathf.random(0,15);
                for (int x = 0; x < tileSize; x++) for (int y = 0; y < tileSize; y++) {
                    if (tile.nearby(x,y) != null) {
                        String cords = x + "" + y;
                        Floor floor = getTextureFromCords(cords, random, floorTiles, tileSize);
                        tile.nearby(x, y).setFloorUnder(floor);
                    }
                }
            }
        }

        if (tile.floor().name.contains("generate")) tile.setFloorUnder(smallFloor);

        Draw.alpha(1.0F);
        this.drawEdges(tile);
        this.drawOverlay(tile);
    }
}
