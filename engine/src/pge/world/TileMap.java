package pge.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import pge.gfx.Canvas;
import pge.gfx.SpriteSheet;

/**
 * A grid of tiles: the classic way to build 2D levels.
 *
 * Tile maps come from the game consoles of the 1980s, which had very little memory:
 * only a few kilobytes for everything on the screen. Storing every pixel of a level
 * was impossible. Instead, the level was cut into small squares ("tiles"), the console
 * stored a few dozen different tile pictures, and the level itself was just a grid of
 * numbers saying which picture goes where. A wall made of a hundred bricks needs only
 * one brick picture. Games still use tile maps today, because they are easy to edit
 * and fast to draw.
 *
 * The map stores one number per cell. The number says which picture of the tile set
 * (a SpriteSheet) to draw there; -1 means "nothing here".
 *
 *   SpriteSheet tiles = SpriteSheet.load("assets/tiles.png", 16, 16);
 *   TileMap level = TileMap.load("assets/level1.csv", tiles);
 *
 * The tile size comes from the sprite sheet, so any size works (8x8, 16x16, 32x24...).
 *
 * THE CSV FORMAT
 * CSV means "comma-separated values". It is plain text: one line per row of the map,
 * with the numbers separated by commas. Any text editor or spreadsheet program can
 * open it, which is why so many programs use it to exchange tables of data.
 *
 *   -1,-1,-1, 7,-1
 *   -1, 3, 3, 3,-1
 *    0, 0, 0, 0, 0
 *
 * This is the same format that the free, open-source Tiled map editor
 * (www.mapeditor.org) writes with File > Export As > CSV: one file per layer, tiles
 * numbered from 0, and -1 for an empty place.
 *
 * COORDINATES
 * Two kinds of positions are used here, so the method names say which one they take:
 *   - columns and rows count tiles:  column 3, row 2
 *   - x and y count pixels in the world: x 48, y 32 (the same place, with 16x16 tiles)
 *
 * SOLID TILES
 * Tell the map which tile numbers are walls with setSolid(number, true). Then ask
 * questions like isSolidAt(x, y) or overlapsSolid(x, y, width, height) to make
 * characters stop at walls. Places outside the map count as solid, so nothing can
 * leave it; change that with setOutsideSolid(false).
 *
 * LAYERS
 * For scenery behind (or in front of) the playing area, use more than one map with
 * the same tile set and draw them in order, background first. Each map drawn later
 * covers the ones before it, the way a painter paints the sky first and the trees on
 * top of it. (This is in fact called the "painter's algorithm".)
 *
 * HOW THE GRID IS STORED
 * The grid is kept in a normal one-dimensional array, row by row, exactly like the
 * pixels of a Canvas: the tile in column c and row r is at index  r * columns + c.
 */
public class TileMap {

    public static final int EMPTY = -1;

    private final int columns;
    private final int rows;
    private final int[] tiles;
    private final SpriteSheet tileSet;
    private final boolean[] solid;
    private boolean outsideSolid = true;

    /** A new map of the given size, completely empty. Fill it with setTile. */
    public TileMap(int columns, int rows, SpriteSheet tileSet) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("A map needs at least 1 column and 1 row, got " + columns + "x" + rows);
        }
        this.columns = columns;
        this.rows = rows;
        this.tileSet = tileSet;
        this.tiles = new int[columns * rows];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = EMPTY;
        }
        this.solid = new boolean[tileSet.getCount()];
    }

    /** Loads a map from a CSV file (see the format above). */
    public static TileMap load(String path, SpriteSheet tileSet) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException("Could not read the map '" + path + "'. "
                    + "Looked for it here: " + Path.of(path).toAbsolutePath());
        }

        // Files.readAllLines reads a whole text file and gives back one String per line.
        // Then we turn every line into an array of numbers, skipping blank lines.
        List<int[]> numberRows = new ArrayList<>();
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",");    // "1,2,3" becomes {"1", "2", "3"}
            int[] numbers = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                try {
                    numbers[i] = Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    throw new RuntimeException(path + ", line " + (lineIndex + 1) + ", value " + (i + 1)
                            + ": '" + part + "' is not a whole number");
                }
                if (numbers[i] < EMPTY || numbers[i] >= tileSet.getCount()) {
                    throw new RuntimeException(path + ", line " + (lineIndex + 1) + ", value " + (i + 1)
                            + ": there is no tile " + numbers[i] + " (the tile set has tiles 0 to "
                            + (tileSet.getCount() - 1) + ", and -1 means empty)");
                }
            }
            if (!numberRows.isEmpty() && numbers.length != numberRows.get(0).length) {
                throw new RuntimeException(path + ", line " + (lineIndex + 1) + " has " + numbers.length
                        + " values, but the first line has " + numberRows.get(0).length
                        + ". Every row of a map must be equally long.");
            }
            numberRows.add(numbers);
        }
        if (numberRows.isEmpty()) {
            throw new RuntimeException("The map '" + path + "' is empty");
        }

        // Then copy them into a new map.
        TileMap map = new TileMap(numberRows.get(0).length, numberRows.size(), tileSet);
        for (int row = 0; row < map.rows; row++) {
            for (int column = 0; column < map.columns; column++) {
                map.setTile(column, row, numberRows.get(row)[column]);
            }
        }
        return map;
    }

    // ------------------------------------------------------------------ size

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getTileWidth() {
        return tileSet.getCellWidth();
    }

    public int getTileHeight() {
        return tileSet.getCellHeight();
    }

    /** Width of the whole map in pixels. */
    public int getWidth() {
        return columns * getTileWidth();
    }

    /** Height of the whole map in pixels. */
    public int getHeight() {
        return rows * getTileHeight();
    }

    public SpriteSheet getTileSet() {
        return tileSet;
    }

    // ------------------------------------------------------------------ tiles by column and row

    /** The tile number at this column and row, or EMPTY (-1) outside the map. */
    public int getTile(int column, int row) {
        if (!isInside(column, row)) {
            return EMPTY;
        }
        return tiles[row * columns + column];
    }

    /** Changes one tile, for example to remove a block the player broke. Outside the map, does nothing. */
    public void setTile(int column, int row, int tile) {
        if (!isInside(column, row)) {
            return;
        }
        if (tile < EMPTY || tile >= tileSet.getCount()) {
            throw new IllegalArgumentException("There is no tile " + tile + " (the tile set has tiles 0 to "
                    + (tileSet.getCount() - 1) + ", and -1 means empty)");
        }
        tiles[row * columns + column] = tile;
    }

    /** True if this column and row are part of the map. */
    public boolean isInside(int column, int row) {
        return column >= 0 && column < columns && row >= 0 && row < rows;
    }

    // ------------------------------------------------------------------ pixels to tiles

    /**
     * The column that contains world position x: x divided by the tile width.
     *
     * We use Math.floorDiv instead of the normal / operator because of negative
     * numbers. Java's / always rounds towards zero, so -1 / 16 is 0, which would put a
     * point just left of the map inside column 0. Math.floorDiv always rounds down, so
     * Math.floorDiv(-1, 16) is -1: correctly outside the map.
     */
    public int columnAt(int x) {
        return Math.floorDiv(x, getTileWidth());
    }

    /** The row that contains world position y. */
    public int rowAt(int y) {
        return Math.floorDiv(y, getTileHeight());
    }

    /** The tile number at world position (x, y). */
    public int getTileAt(int x, int y) {
        return getTile(columnAt(x), rowAt(y));
    }

    // ------------------------------------------------------------------ solid tiles

    /** Marks a tile number as a wall (true) or as scenery you can walk through (false). */
    public void setSolid(int tile, boolean isSolid) {
        if (tile < 0 || tile >= solid.length) {
            throw new IllegalArgumentException("There is no tile " + tile + " (the tile set has tiles 0 to "
                    + (solid.length - 1) + ")");
        }
        solid[tile] = isSolid;
    }

    /** True if this tile number was marked as a wall. */
    public boolean isSolid(int tile) {
        if (tile < 0 || tile >= solid.length) {
            return false;
        }
        return solid[tile];
    }

    /** Whether the area outside the map counts as a wall. True by default. */
    public void setOutsideSolid(boolean isSolid) {
        outsideSolid = isSolid;
    }

    /** True if the tile at this column and row is a wall. */
    public boolean isSolidTile(int column, int row) {
        if (!isInside(column, row)) {
            return outsideSolid;
        }
        return isSolid(getTile(column, row));
    }

    /** True if world position (x, y) is inside a wall. */
    public boolean isSolidAt(int x, int y) {
        return isSolidTile(columnAt(x), rowAt(y));
    }

    /**
     * True if any part of this rectangle (in world pixels) touches a wall.
     * This is the question to ask before moving a character: "if I went there,
     * would I be inside a wall?"
     *
     * It works out which columns and rows the rectangle's corners are in, and checks
     * every tile in between. A 10x13 character on 16x16 tiles touches at most 2 by 2
     * tiles, so this is quick.
     */
    public boolean overlapsSolid(int x, int y, int width, int height) {
        int firstColumn = columnAt(x);
        int lastColumn = columnAt(x + width - 1);
        int firstRow = rowAt(y);
        int lastRow = rowAt(y + height - 1);
        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (isSolidTile(column, row)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ drawing

    /**
     * Draws the map, with its top-left corner at world position (0, 0).
     *
     * Only the tiles the camera can see are drawn: with a 640x360 screen and 16x16
     * tiles that is about 41 x 24 = 984 tiles, however big the map is. Not drawing what
     * cannot be seen is called "culling", and every game engine does it in some form.
     */
    public void draw(Canvas canvas) {
        int tileWidth = getTileWidth();
        int tileHeight = getTileHeight();

        // Which columns and rows are on screen? Then cut that range to the map's edges.
        int firstColumn = Math.max(0, columnAt(canvas.getCameraX()));
        int lastColumn = Math.min(columns - 1, columnAt(canvas.getCameraX() + canvas.getWidth() - 1));
        int firstRow = Math.max(0, rowAt(canvas.getCameraY()));
        int lastRow = Math.min(rows - 1, rowAt(canvas.getCameraY() + canvas.getHeight() - 1));

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                int tile = tiles[row * columns + column];
                if (tile != EMPTY) {
                    canvas.drawBitmap(tileSet.get(tile), column * tileWidth, row * tileHeight);
                }
            }
        }
    }
}
