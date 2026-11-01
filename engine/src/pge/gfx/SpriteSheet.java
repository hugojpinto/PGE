package pge.gfx;

/**
 * One image holding many pictures of the same size ("cells"), arranged in a grid.
 *
 * A "sprite" is a small picture that moves around the screen on its own, such as a
 * character or a coin. The word was used this way by engineers at Texas Instruments
 * for a video chip they designed at the end of the 1970s, and it stuck. Games usually
 * keep all their sprites in one image: every animation frame and every tile, side by
 * side. One file is simpler to draw in and to load than hundreds of tiny ones.
 * (In 3D games the same idea is called a "texture atlas".)
 *
 *   SpriteSheet sheet = SpriteSheet.load("assets/sprites.png", 16, 16);
 *   Bitmap hero = sheet.get(0);
 *
 * Cells are numbered left to right, then top to bottom, the same order in which you
 * read a page. In a sheet 4 cells wide:
 *
 *    0  1  2  3
 *    4  5  6  7
 *    8  9 10 11
 *
 * So cell number n is in column n % columns and row n / columns.
 *
 * Any cell size is fine (16x16, 24x32, 48x48...). If the image is not an exact
 * multiple of the cell size, the leftover strip on the right or at the bottom is
 * ignored.
 */
public class SpriteSheet {

    private final int cellWidth;
    private final int cellHeight;
    private final int columns;
    private final int rows;
    private final Bitmap[] cells;

    /** Cuts an already loaded bitmap into cells. */
    public SpriteSheet(Bitmap image, int cellWidth, int cellHeight) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            throw new IllegalArgumentException("Cell size must be positive, got " + cellWidth + "x" + cellHeight);
        }
        if (cellWidth > image.getWidth() || cellHeight > image.getHeight()) {
            throw new IllegalArgumentException("Cells of " + cellWidth + "x" + cellHeight
                    + " do not fit in an image of " + image.getWidth() + "x" + image.getHeight());
        }
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.columns = image.getWidth() / cellWidth;
        this.rows = image.getHeight() / cellHeight;

        // Cut every cell out once, now. Then get() only has to look up an array, which is
        // instant, instead of copying pixels every time a picture is drawn.
        cells = new Bitmap[columns * rows];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                cells[row * columns + col] = image.crop(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
            }
        }
    }

    /** Loads a PNG and cuts it into cells. */
    public static SpriteSheet load(String path, int cellWidth, int cellHeight) {
        return new SpriteSheet(Bitmap.load(path), cellWidth, cellHeight);
    }

    /** Loads a PNG, treating colorKey as transparent, and cuts it into cells. */
    public static SpriteSheet load(String path, int cellWidth, int cellHeight, int colorKey) {
        return new SpriteSheet(Bitmap.load(path, colorKey), cellWidth, cellHeight);
    }

    /** The cell with this number (counting left to right, then down, from 0). */
    public Bitmap get(int index) {
        if (index < 0 || index >= cells.length) {
            throw new IndexOutOfBoundsException("This sprite sheet has cells 0 to " + (cells.length - 1)
                    + ", there is no cell " + index);
        }
        return cells[index];
    }

    /** The cell at this column and row of the grid. */
    public Bitmap get(int column, int row) {
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException("This sprite sheet is " + columns + " columns by " + rows
                    + " rows, there is no cell at column " + column + ", row " + row);
        }
        return cells[row * columns + column];
    }

    /** Several cells in a row, for example the frames of an animation: getFrames(8, 4) gives cells 8, 9, 10, 11. */
    public Bitmap[] getFrames(int first, int count) {
        Bitmap[] frames = new Bitmap[count];
        for (int i = 0; i < count; i++) {
            frames[i] = get(first + i);
        }
        return frames;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    /** How many cells the sheet has in total. */
    public int getCount() {
        return cells.length;
    }
}
