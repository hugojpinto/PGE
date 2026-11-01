import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Draws PGE's built-in pixel art with code and saves it as PNG files:
 *
 *   engine/src/pge/gfx/font.png          the default font (ASCII characters 32 to 127)
 *   games/demo/assets/sprites.png        the demo's sprite sheet (16x16 cells)
 *   games/platformer/assets/sprites.png  the same sprites, for the platformer
 *   games/platformer/assets/tiles.png    the platformer's tile set (16x16 cells)
 *   games/platformer/assets/level1.csv   the platformer's level, main layer
 *   games/platformer/assets/level1-back.csv  ... and its background layer
 *   games/platformer-scenes/assets/...   the same, plus slimes and the hero's start in the level
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
        makeTiles();
        makeLevel();
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

        for (int frame = 0; frame < 4; frame++) {
            drawHero(sheet, frame, frame);
        }
        drawSlime(sheet, 4, 0);
        drawSlime(sheet, 5, 1);
        for (int frame = 0; frame < 4; frame++) {
            drawCoin(sheet, 8 + frame, frame);
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
        save(sheet, "games/platformer/assets/sprites.png");
        save(sheet, "games/platformer-scenes/assets/sprites.png");
    }

    /** Hero: a round blob. Frames 1 and 3 lift the body and move the feet apart. */
    static void drawHero(BufferedImage sheet, int cell, int frame) {
        int[] leftFootX = { 4, 2, 4, 6 };
        int[] rightFootX = { 9, 11, 9, 7 };
        int lift = 0;
        if (frame == 1 || frame == 3) {
            lift = 1;
        }
        rect(sheet, cell, leftFootX[frame], 13, 3, 2, HERO_FEET);
        rect(sheet, cell, rightFootX[frame], 13, 3, 2, HERO_FEET);
        ellipse(sheet, cell, 7.5, 8.0 - lift, 6.0, 5.5, HERO_BODY, 15);
        ellipse(sheet, cell, 5.0, 6.0 - lift, 2.0, 1.5, HERO_LIGHT, 15);
        rect(sheet, cell, 8, 5 - lift, 2, 3, EYE_WHITE);
        rect(sheet, cell, 11, 5 - lift, 2, 3, EYE_WHITE);
        rect(sheet, cell, 9, 6 - lift, 1, 2, PUPIL);
        rect(sheet, cell, 12, 6 - lift, 1, 2, PUPIL);
        outline(sheet, cell, OUTLINE);
    }

    /** Slime: a dome with a flat bottom. Frame 1 is squashed. */
    static void drawSlime(BufferedImage sheet, int cell, int frame) {
        double[] slimeWidth = { 6.5, 7.2 };
        double[] slimeHeight = { 8.0, 6.5 };
        ellipse(sheet, cell, 7.5, 14.5, slimeWidth[frame], slimeHeight[frame], SLIME_BODY, 14);
        ellipse(sheet, cell, 5.0, 14.5 - slimeHeight[frame] + 2.5, 1.5, 1.0, SLIME_LIGHT, 14);
        int eyeY = (int) (14.5 - slimeHeight[frame] + 3);
        rect(sheet, cell, 6, eyeY, 1, 2, PUPIL);
        rect(sheet, cell, 9, eyeY, 1, 2, PUPIL);
        outline(sheet, cell, OUTLINE);
    }

    /** Coin: an ellipse that gets thinner and wider again, so it looks like it spins. */
    static void drawCoin(BufferedImage sheet, int cell, int frame) {
        double[] coinWidth = { 5.0, 3.5, 1.2, 3.5 };
        ellipse(sheet, cell, 7.5, 7.5, coinWidth[frame], 6.0, COIN_GOLD, 15);
        if (coinWidth[frame] > 2) {
            ellipse(sheet, cell, 7.5, 7.5, coinWidth[frame] * 0.5, 3.5, COIN_SHADE, 15);
            rect(sheet, cell, (int) (7.5 - coinWidth[frame] * 0.6), 4, 1, 3, COIN_SHINE);
        }
        outline(sheet, cell, COIN_OUTLINE);
    }

    // ==================================================================== TILES
    //
    // A 4x3 sheet of 16x16 tiles:
    //   0 grass   1 dirt   2 brick   3 stone          (solid)
    //   4 wall    5 bush   6 flower  7 coin marker    (scenery)
    //   8 slime marker     9 hero marker              (used by platformer-scenes)
    //
    // Markers show where the level designer wants something. The game replaces
    // them with real objects when the level loads.

    static void makeTiles() throws IOException {
        BufferedImage sheet = new BufferedImage(SHEET_COLUMNS * CELL, 3 * CELL, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(42);   // fixed seed: the same "random" speckles every time

        // 1 Dirt: brown with darker and lighter speckles
        speckled(sheet, 1, 0xFF8C5A34, 0xFF6B4226, 0xFFA8703F, random);

        // 0 Grass: dirt with a green top and an uneven edge
        speckled(sheet, 0, 0xFF8C5A34, 0xFF6B4226, 0xFFA8703F, random);
        rect(sheet, 0, 0, 0, 16, 4, 0xFF4CC04A);
        rect(sheet, 0, 0, 0, 16, 1, 0xFF8BE070);
        for (int x = 0; x < CELL; x++) {
            if (x % 3 != 1) {
                set(sheet, 0, x, 4, 0xFF2F8A3A);
            }
        }

        // 2 Brick: red bricks, every other row shifted by half a brick
        rect(sheet, 2, 0, 0, 16, 16, 0xFFB0503A);
        for (int y = 0; y < CELL; y = y + 8) {
            rect(sheet, 2, 0, y, 16, 1, 0xFF6E2E22);
            rect(sheet, 2, 0, y + 1, 16, 1, 0xFFCC6A50);
        }
        rect(sheet, 2, 0, 1, 1, 7, 0xFF6E2E22);
        rect(sheet, 2, 8, 9, 1, 7, 0xFF6E2E22);

        // 3 Stone: a gray block, light on the top-left edges, dark on the bottom-right
        rect(sheet, 3, 0, 0, 16, 16, 0xFF8B8B9E);
        rect(sheet, 3, 0, 0, 16, 1, 0xFFC9C9D6);
        rect(sheet, 3, 0, 0, 1, 16, 0xFFC9C9D6);
        rect(sheet, 3, 0, 15, 16, 1, 0xFF4A4A5E);
        rect(sheet, 3, 15, 0, 1, 16, 0xFF4A4A5E);
        rect(sheet, 3, 4, 5, 3, 1, 0xFF6E6E82);
        rect(sheet, 3, 9, 10, 2, 1, 0xFF6E6E82);

        // 4 Wall: dark bricks for backgrounds
        rect(sheet, 4, 0, 0, 16, 16, 0xFF3B3B5A);
        for (int y = 0; y < CELL; y = y + 8) {
            rect(sheet, 4, 0, y, 16, 1, 0xFF2A2A44);
        }
        rect(sheet, 4, 0, 1, 1, 7, 0xFF2A2A44);
        rect(sheet, 4, 8, 9, 1, 7, 0xFF2A2A44);

        // 5 Bush: three round blobs sitting on the bottom edge
        ellipse(sheet, 5, 4.5, 11.0, 4.5, 4.5, 0xFF3FA040, 15);
        ellipse(sheet, 5, 10.5, 10.0, 5.0, 5.5, 0xFF3FA040, 15);
        ellipse(sheet, 5, 8.0, 13.5, 7.5, 3.0, 0xFF3FA040, 15);
        ellipse(sheet, 5, 9.0, 7.5, 2.0, 1.5, 0xFF7FD06A, 15);
        outline(sheet, 5, 0xFF1F5A2A);

        // 6 Flower: a stem with a pink head
        rect(sheet, 6, 7, 9, 1, 7, 0xFF2F8A3A);
        rect(sheet, 6, 8, 12, 2, 1, 0xFF2F8A3A);
        ellipse(sheet, 6, 7.5, 6.5, 2.8, 2.8, 0xFFF283B4, 15);
        rect(sheet, 6, 7, 6, 1, 1, 0xFFF7D83B);
        outline(sheet, 6, 0xFF6E2A4A);

        // 7, 8, 9 Markers: a coin, a slime, the hero's starting spot
        drawCoin(sheet, 7, 0);
        drawSlime(sheet, 8, 0);
        drawHero(sheet, 9, 0);

        save(sheet, "games/platformer/assets/tiles.png");
        save(sheet, "games/platformer-scenes/assets/tiles.png");
    }

    /** Fills a tile with a base color and sprinkles darker and lighter pixels over it. */
    static void speckled(BufferedImage sheet, int cell, int base, int dark, int light, Random random) {
        rect(sheet, cell, 0, 0, CELL, CELL, base);
        for (int i = 0; i < 18; i++) {
            set(sheet, cell, random.nextInt(CELL), random.nextInt(CELL), dark);
        }
        for (int i = 0; i < 10; i++) {
            set(sheet, cell, random.nextInt(CELL), random.nextInt(CELL), light);
        }
    }

    // ==================================================================== LEVEL
    //
    // The level is 80 x 28 tiles. It is built by writing short strings into a grid
    // of characters, one character per tile:
    //
    //   G grass  D dirt  B brick  S stone  b bush  f flower  c coin  (space) empty
    //   e slime  p hero start   (only in the platformer-scenes version)

    static final int LEVEL_COLUMNS = 80;
    static final int LEVEL_ROWS = 28;

    static void makeLevel() throws IOException {
        makeLevel(false, "games/platformer/assets/");
        makeLevel(true, "games/platformer-scenes/assets/");
    }

    static void makeLevel(boolean withCharacters, String folder) throws IOException {
        char[][] main = emptyGrid();
        char[][] back = emptyGrid();

        // Ground, with two dips
        for (int row = 24; row < LEVEL_ROWS; row++) {
            char ground = 'D';
            if (row == 24) {
                ground = 'G';
            }
            put(main, row, 0, repeat(ground, LEVEL_COLUMNS));
        }
        put(main, 24, 12, "   ");
        put(main, 25, 12, "GGG");
        put(main, 24, 36, "   ");
        put(main, 25, 36, "GGG");

        // A staircase of platforms up to the sky bridge
        put(main, 20, 5, "SSSSS");
        put(main, 19, 5, "c c c");
        put(main, 17, 12, "SSS");
        put(main, 16, 13, "c");
        put(main, 14, 18, "SSSSS");
        put(main, 13, 19, "c c");
        put(main, 11, 26, "SSS");
        put(main, 10, 27, "c");
        put(main, 8, 33, "SSSSS");
        put(main, 7, 34, "c c");
        put(main, 8, 42, "SSS");
        put(main, 7, 43, "c");
        put(main, 4, 48, "BBBBBBBBBBBBBBBB");
        put(main, 3, 50, "c   c   c   c");

        // Low platforms
        put(main, 21, 22, "BBBBBBB");
        put(main, 20, 23, "c c c");
        put(main, 18, 33, "SSS");
        put(main, 17, 34, "c");
        put(main, 21, 46, "SSSSSSS");
        put(main, 20, 47, "c c c");

        // The castle: a roof, two walls with doorways at the bottom, and a ledge inside
        put(main, 14, 62, "BBBBBBBBBBBB");
        put(main, 13, 64, "c c c c");
        for (int row = 15; row <= 21; row++) {
            put(main, row, 62, "B");
            put(main, row, 73, "B");
        }
        put(main, 20, 65, "BBBBBB");
        put(main, 19, 66, "c  c");
        put(main, 23, 66, "cccc");
        for (int row = 15; row <= 23; row++) {
            put(back, row, 63, repeat('w', 10));
        }

        // Scenery
        put(main, 23, 2, "b");
        put(main, 23, 8, "f");
        put(main, 23, 19, "f");
        put(main, 23, 30, "b");
        put(main, 23, 44, "f");
        put(main, 23, 57, "b");
        put(main, 23, 77, "f");

        if (withCharacters) {
            put(main, 23, 1, "p");
            put(main, 23, 20, "e");
            put(main, 23, 42, "e");
            put(main, 23, 55, "e");
            put(main, 20, 48, "e");
            put(main, 13, 20, "e");
            put(main, 23, 71, "e");
        }

        saveLevel(main, folder + "level1.csv");
        saveLevel(back, folder + "level1-back.csv");
    }

    static char[][] emptyGrid() {
        char[][] grid = new char[LEVEL_ROWS][LEVEL_COLUMNS];
        for (int row = 0; row < LEVEL_ROWS; row++) {
            for (int column = 0; column < LEVEL_COLUMNS; column++) {
                grid[row][column] = ' ';
            }
        }
        return grid;
    }

    static void put(char[][] grid, int row, int column, String text) {
        for (int i = 0; i < text.length(); i++) {
            grid[row][column + i] = text.charAt(i);
        }
    }

    static String repeat(char c, int count) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < count; i++) {
            text.append(c);
        }
        return text.toString();
    }

    /** Writes the grid as CSV: one line per row, tile numbers separated by commas. */
    static void saveLevel(char[][] grid, String path) throws IOException {
        String letters = "GDBSwbfcep";    // the position of each letter is its tile number
        PrintWriter out = new PrintWriter(path);
        for (int row = 0; row < LEVEL_ROWS; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < LEVEL_COLUMNS; column++) {
                if (column > 0) {
                    line.append(',');
                }
                line.append(letters.indexOf(grid[row][column]));   // ' ' is not in letters: -1, empty
            }
            out.println(line);
        }
        out.close();
        System.out.println("Wrote " + path);
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
