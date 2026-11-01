import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Draws PGE's built-in pixel art with code and saves it as PNG files:
 *
 *   engine/src/pge/gfx/font.png    the default font (ASCII characters 32 to 127)
 *   games/demo/assets/sprites.png  the demo's sprite sheet (16x16 cells)
 *
 * Run it from the PGE folder:   java tools/MakeAssets.java
 *
 * You only need this to re-create those files. To change the art, you can also
 * just open the PNGs in any pixel editor (Aseprite, Piskel, GIMP...) and paint.
 */
public class MakeAssets {

    public static void main(String[] args) throws IOException {
        makeFont();
        makeSprites();
    }

    // ===================================================================== FONT
    //
    // Each character is drawn below as rows of text: '#' is ink, '.' is empty.
    // Capitals and digits use rows 0-6. Row 7 is for the tails of g, j, p, q, y.
    // Glyphs can be narrower than 5: the engine measures each one's width itself.
    //
    // The font image is a grid of 16 columns x 6 rows of 8x8 cells, holding the
    // characters 32 (space) to 127 in order. Character 127 is a box, shown for any
    // character the font does not have.

    static final int FONT_CELL = 8;
    static final String[][] GLYPHS = new String[128][];

    static void glyph(char c, String... rows) {
        GLYPHS[c] = rows;
    }

    static void makeFont() throws IOException {
        glyph(' ');
        glyph('!', "#", "#", "#", "#", "#", ".", "#");
        glyph('"', "#.#", "#.#");
        glyph('#', ".#.#.", ".#.#.", "#####", ".#.#.", "#####", ".#.#.", ".#.#.");
        glyph('$', "..#..", ".####", "#.#..", ".###.", "..#.#", "####.", "..#..");
        glyph('%', "##..#", "##..#", "...#.", "..#..", ".#...", "#..##", "#..##");
        glyph('&', ".##..", "#..#.", "#.#..", ".#...", "#.#.#", "#..#.", ".##.#");
        glyph('\'', "#", "#");
        glyph('(', ".#", "#.", "#.", "#.", "#.", "#.", ".#");
        glyph(')', "#.", ".#", ".#", ".#", ".#", ".#", "#.");
        glyph('*', ".....", "..#..", "#.#.#", ".###.", "#.#.#", "..#..");
        glyph('+', ".....", "..#..", "..#..", "#####", "..#..", "..#..");
        glyph(',', "..", "..", "..", "..", "..", ".#", ".#", "#.");
        glyph('-', "....", "....", "....", "####");
        glyph('.', ".", ".", ".", ".", ".", ".", "#");
        glyph('/', "....#", "....#", "...#.", "..#..", ".#...", "#....", "#....");
        glyph('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.");
        glyph('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.");
        glyph('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####");
        glyph('3', ".###.", "#...#", "....#", "..##.", "....#", "#...#", ".###.");
        glyph('4', "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#.");
        glyph('5', "#####", "#....", "####.", "....#", "....#", "#...#", ".###.");
        glyph('6', "..##.", ".#...", "#....", "####.", "#...#", "#...#", ".###.");
        glyph('7', "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#...");
        glyph('8', ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###.");
        glyph('9', ".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##..");
        glyph(':', ".", ".", "#", ".", ".", "#");
        glyph(';', "..", "..", ".#", "..", "..", ".#", ".#", "#.");
        glyph('<', "...#", "..#.", ".#..", "#...", ".#..", "..#.", "...#");
        glyph('=', ".....", ".....", "#####", ".....", "#####");
        glyph('>', "#...", ".#..", "..#.", "...#", "..#.", ".#..", "#...");
        glyph('?', ".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#..");
        glyph('@', ".###.", "#...#", "#.###", "#.#.#", "#.###", "#....", ".###.");
        glyph('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        glyph('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.");
        glyph('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.");
        glyph('D', "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####.");
        glyph('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####");
        glyph('F', "#####", "#....", "#....", "####.", "#....", "#....", "#....");
        glyph('G', ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####");
        glyph('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        glyph('I', "###", ".#.", ".#.", ".#.", ".#.", ".#.", "###");
        glyph('J', "..###", "...#.", "...#.", "...#.", "#..#.", "#..#.", ".##..");
        glyph('K', "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#");
        glyph('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####");
        glyph('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#");
        glyph('N', "#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#");
        glyph('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        glyph('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....");
        glyph('Q', ".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#");
        glyph('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#");
        glyph('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.");
        glyph('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
        glyph('U', "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        glyph('V', "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#..");
        glyph('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#.");
        glyph('X', "#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#");
        glyph('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..");
        glyph('Z', "#####", "....#", "...#.", "..#..", ".#...", "#....", "#####");
        glyph('[', "##", "#.", "#.", "#.", "#.", "#.", "##");
        glyph('\\', "#....", "#....", ".#...", "..#..", "...#.", "....#", "....#");
        glyph(']', "##", ".#", ".#", ".#", ".#", ".#", "##");
        glyph('^', "..#..", ".#.#.", "#...#");
        glyph('_', ".....", ".....", ".....", ".....", ".....", ".....", ".....", "#####");
        glyph('`', "#.", ".#");
        glyph('a', ".....", ".....", ".###.", "....#", ".####", "#...#", ".####");
        glyph('b', "#....", "#....", "####.", "#...#", "#...#", "#...#", "####.");
        glyph('c', ".....", ".....", ".####", "#....", "#....", "#....", ".####");
        glyph('d', "....#", "....#", ".####", "#...#", "#...#", "#...#", ".####");
        glyph('e', ".....", ".....", ".###.", "#...#", "#####", "#....", ".####");
        glyph('f', "..##", ".#..", "####", ".#..", ".#..", ".#..", ".#..");
        glyph('g', ".....", ".....", ".####", "#...#", "#...#", ".####", "....#", ".###.");
        glyph('h', "#....", "#....", "####.", "#...#", "#...#", "#...#", "#...#");
        glyph('i', "#", ".", "#", "#", "#", "#", "#");
        glyph('j', "..#", "...", "..#", "..#", "..#", "..#", "#.#", ".#.");
        glyph('k', "#...", "#...", "#..#", "#.#.", "##..", "#.#.", "#..#");
        glyph('l', "#.", "#.", "#.", "#.", "#.", "#.", ".#");
        glyph('m', ".....", ".....", "##.#.", "#.#.#", "#.#.#", "#.#.#", "#.#.#");
        glyph('n', ".....", ".....", "####.", "#...#", "#...#", "#...#", "#...#");
        glyph('o', ".....", ".....", ".###.", "#...#", "#...#", "#...#", ".###.");
        glyph('p', ".....", ".....", "####.", "#...#", "#...#", "####.", "#....", "#....");
        glyph('q', ".....", ".....", ".####", "#...#", "#...#", ".####", "....#", "....#");
        glyph('r', "....", "....", "#.##", "##..", "#...", "#...", "#...");
        glyph('s', ".....", ".....", ".####", "#....", ".###.", "....#", "####.");
        glyph('t', ".#..", ".#..", "####", ".#..", ".#..", ".#..", "..##");
        glyph('u', ".....", ".....", "#...#", "#...#", "#...#", "#...#", ".####");
        glyph('v', ".....", ".....", "#...#", "#...#", "#...#", ".#.#.", "..#..");
        glyph('w', ".....", ".....", "#...#", "#...#", "#.#.#", "#.#.#", ".#.#.");
        glyph('x', ".....", ".....", "#...#", ".#.#.", "..#..", ".#.#.", "#...#");
        glyph('y', ".....", ".....", "#...#", "#...#", "#...#", ".####", "....#", ".###.");
        glyph('z', ".....", ".....", "#####", "...#.", "..#..", ".#...", "#####");
        glyph('{', "..#", ".#.", ".#.", "#..", ".#.", ".#.", "..#");
        glyph('|', "#", "#", "#", "#", "#", "#", "#");
        glyph('}', "#..", ".#.", ".#.", "..#", ".#.", ".#.", "#..");
        glyph('~', ".....", ".....", ".#...", "#.#.#", "...#.");
        glyph((char) 127, "#####", "#...#", "#...#", "#...#", "#...#", "#...#", "#####");

        int columns = 16;
        int rows = 6;
        BufferedImage image = new BufferedImage(columns * FONT_CELL, rows * FONT_CELL, BufferedImage.TYPE_INT_ARGB);
        for (int c = 32; c < 128; c++) {
            String[] glyphRows = GLYPHS[c];
            if (glyphRows == null) {
                throw new IllegalStateException("No glyph for character " + c);
            }
            int cellX = ((c - 32) % columns) * FONT_CELL;
            int cellY = ((c - 32) / columns) * FONT_CELL;
            for (int y = 0; y < glyphRows.length; y++) {
                for (int x = 0; x < glyphRows[y].length(); x++) {
                    if (glyphRows[y].charAt(x) == '#') {
                        image.setRGB(cellX + x, cellY + y, 0xFFFFFFFF);
                    }
                }
            }
        }
        save(image, "engine/src/pge/gfx/font.png");
    }

    // ================================================================== SPRITES
    //
    // A 4x4 sheet of 16x16 cells:
    //   0-3   hero walking (facing right)
    //   4-5   slime wobbling
    //   8-11  coin spinning
    //   12    heart

    static final int CELL = 16;
    static final int SHEET_COLUMNS = 4;

    static final int OUTLINE      = 0xFF14142A;
    static final int HERO_BODY    = 0xFF3C78E0;
    static final int HERO_LIGHT   = 0xFF7FA8F2;
    static final int HERO_FEET    = 0xFF283C8C;
    static final int EYE_WHITE    = 0xFFFFFFFF;
    static final int PUPIL        = 0xFF14142A;
    static final int SLIME_BODY   = 0xFF4CC04A;
    static final int SLIME_LIGHT  = 0xFF9BE07A;
    static final int COIN_GOLD    = 0xFFF7D83B;
    static final int COIN_SHADE   = 0xFFE0A020;
    static final int COIN_SHINE   = 0xFFFFF6B0;
    static final int COIN_OUTLINE = 0xFF6B3A1A;
    static final int HEART_RED    = 0xFFE0403F;
    static final int HEART_LIGHT  = 0xFFF59A9A;
    static final int HEART_DARK   = 0xFF5A1020;

    static void makeSprites() throws IOException {
        BufferedImage sheet = new BufferedImage(SHEET_COLUMNS * CELL, 4 * CELL, BufferedImage.TYPE_INT_ARGB);

        // Hero: a round blob. Frames 1 and 3 lift the body and move the feet apart.
        int[] leftFootX = { 4, 2, 4, 6 };
        int[] rightFootX = { 9, 11, 9, 7 };
        for (int frame = 0; frame < 4; frame++) {
            int lift = 0;
            if (frame == 1 || frame == 3) {
                lift = 1;
            }
            rect(sheet, frame, leftFootX[frame], 13, 3, 2, HERO_FEET);
            rect(sheet, frame, rightFootX[frame], 13, 3, 2, HERO_FEET);
            ellipse(sheet, frame, 7.5, 8.0 - lift, 6.0, 5.5, HERO_BODY, 15);
            ellipse(sheet, frame, 5.0, 6.0 - lift, 2.0, 1.5, HERO_LIGHT, 15);
            rect(sheet, frame, 8, 5 - lift, 2, 3, EYE_WHITE);
            rect(sheet, frame, 11, 5 - lift, 2, 3, EYE_WHITE);
            rect(sheet, frame, 9, 6 - lift, 1, 2, PUPIL);
            rect(sheet, frame, 12, 6 - lift, 1, 2, PUPIL);
            outline(sheet, frame, OUTLINE);
        }

        // Slime: a dome with a flat bottom. Frame 5 is squashed.
        double[] slimeWidth = { 6.5, 7.2 };
        double[] slimeHeight = { 8.0, 6.5 };
        for (int i = 0; i < 2; i++) {
            int cell = 4 + i;
            ellipse(sheet, cell, 7.5, 14.5, slimeWidth[i], slimeHeight[i], SLIME_BODY, 14);
            ellipse(sheet, cell, 5.0, 14.5 - slimeHeight[i] + 2.5, 1.5, 1.0, SLIME_LIGHT, 14);
            int eyeY = (int) (14.5 - slimeHeight[i] + 3);
            rect(sheet, cell, 6, eyeY, 1, 2, PUPIL);
            rect(sheet, cell, 9, eyeY, 1, 2, PUPIL);
            outline(sheet, cell, OUTLINE);
        }

        // Coin: an ellipse that gets thinner and wider again, so it looks like it spins.
        double[] coinWidth = { 5.0, 3.5, 1.2, 3.5 };
        for (int i = 0; i < 4; i++) {
            int cell = 8 + i;
            ellipse(sheet, cell, 7.5, 7.5, coinWidth[i], 6.0, COIN_GOLD, 15);
            if (coinWidth[i] > 2) {
                ellipse(sheet, cell, 7.5, 7.5, coinWidth[i] * 0.5, 3.5, COIN_SHADE, 15);
                rect(sheet, cell, (int) (7.5 - coinWidth[i] * 0.6), 4, 1, 3, COIN_SHINE);
            }
            outline(sheet, cell, COIN_OUTLINE);
        }

        // Heart: two circles on top of a triangle.
        int heart = 12;
        ellipse(sheet, heart, 5.0, 5.5, 3.2, 3.2, HEART_RED, 15);
        ellipse(sheet, heart, 10.0, 5.5, 3.2, 3.2, HEART_RED, 15);
        for (int y = 6; y <= 13; y++) {
            int inset = y - 6;
            rect(sheet, heart, 2 + inset, y, 12 - 2 * inset, 1, HEART_RED);
        }
        rect(sheet, heart, 4, 4, 2, 2, HEART_LIGHT);
        outline(sheet, heart, HEART_DARK);

        save(sheet, "games/demo/assets/sprites.png");
    }

    /** Fills an ellipse inside one cell, but not below row maxY. */
    static void ellipse(BufferedImage sheet, int cell, double centerX, double centerY,
                        double radiusX, double radiusY, int color, int maxY) {
        for (int y = 0; y < CELL && y <= maxY; y++) {
            for (int x = 0; x < CELL; x++) {
                double dx = (x + 0.5 - centerX) / radiusX;
                double dy = (y + 0.5 - centerY) / radiusY;
                if (dx * dx + dy * dy <= 1.0) {
                    set(sheet, cell, x, y, color);
                }
            }
        }
    }

    static void rect(BufferedImage sheet, int cell, int left, int top, int width, int height, int color) {
        for (int y = top; y < top + height; y++) {
            for (int x = left; x < left + width; x++) {
                set(sheet, cell, x, y, color);
            }
        }
    }

    /** Paints every empty pixel that touches a painted one, giving the sprite a 1-pixel border. */
    static void outline(BufferedImage sheet, int cell, int color) {
        boolean[][] mark = new boolean[CELL][CELL];
        for (int y = 0; y < CELL; y++) {
            for (int x = 0; x < CELL; x++) {
                if (get(sheet, cell, x, y) == 0) {
                    boolean touches = isPainted(sheet, cell, x - 1, y) || isPainted(sheet, cell, x + 1, y)
                            || isPainted(sheet, cell, x, y - 1) || isPainted(sheet, cell, x, y + 1);
                    mark[y][x] = touches;
                }
            }
        }
        for (int y = 0; y < CELL; y++) {
            for (int x = 0; x < CELL; x++) {
                if (mark[y][x]) {
                    set(sheet, cell, x, y, color);
                }
            }
        }
    }

    static boolean isPainted(BufferedImage sheet, int cell, int x, int y) {
        if (x < 0 || x >= CELL || y < 0 || y >= CELL) {
            return false;
        }
        return get(sheet, cell, x, y) != 0;
    }

    static void set(BufferedImage sheet, int cell, int x, int y, int color) {
        if (x < 0 || x >= CELL || y < 0 || y >= CELL) {
            return;
        }
        sheet.setRGB((cell % SHEET_COLUMNS) * CELL + x, (cell / SHEET_COLUMNS) * CELL + y, color);
    }

    static int get(BufferedImage sheet, int cell, int x, int y) {
        return sheet.getRGB((cell % SHEET_COLUMNS) * CELL + x, (cell / SHEET_COLUMNS) * CELL + y);
    }

    static void save(BufferedImage image, String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
        System.out.println("Wrote " + path);
    }
}
