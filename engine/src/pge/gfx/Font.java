package pge.gfx;

/**
 * A pixel font: a sprite sheet with one picture (a "glyph") for each character.
 * Fonts made of small pictures like this are called "bitmap fonts". Early computers,
 * terminals and game consoles all used them, because drawing letters from curves, the
 * way modern fonts do, needed far more computing power.
 *
 * WHICH CHARACTERS
 * Computers store each character as a number. The standard list of numbers for
 * English letters, digits and punctuation is ASCII (the American Standard Code for
 * Information Interchange), first published in 1963. Java's char values use the same
 * numbers for these characters, so 'A' is 65, 'a' is 97 and a space is 32.
 *
 * ASCII codes 0 to 31 are not letters but "control characters", left over from
 * teleprinters: instructions such as "start a new line" or "ring the bell". So a font
 * starts at 32, the space. The image holds the characters 32 to 126 in order, 16 per
 * row, plus character 127 in the last place. In ASCII, 127 is "DEL" (delete), another
 * control character, so we borrow its place for a box that is shown for any character
 * the font does not have, such as letters with accents.
 *
 *    !"#$%&'()*+,-./
 *   0123456789:;<=>?
 *   @ABCDEFGHIJKLMNO
 *   PQRSTUVWXYZ[\]^_
 *   `abcdefghijklmno
 *   pqrstuvwxyz{|}~[box]
 *
 * Paint the letters in any solid colour on a transparent background. The colour does
 * not matter: the canvas draws every visible pixel in the colour you ask for.
 *
 * Letters are "proportional": an i takes less room than an m, as in printed books.
 * (Typewriters, and the fonts programmers use for code, are "monospaced": every
 * letter takes the same room.) The font measures how wide each letter is when it
 * loads, by finding its rightmost visible pixel.
 *
 * The engine comes with a default 8x8 font. To use your own:
 *
 *   Font big = Font.load("assets/bigfont.png", 12, 16);
 *   canvas.setFont(big);
 */
public class Font {

    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 127;
    private static final int MISSING_CHAR = 127;
    private static final int COLUMNS = 16;

    private static Font defaultFont;

    private final Bitmap[] glyphs = new Bitmap[LAST_CHAR + 1];
    private final int[] advances = new int[LAST_CHAR + 1];
    private final int cellHeight;

    /** Builds a font from an image laid out as described above. */
    public Font(Bitmap image, int cellWidth, int cellHeight) {
        SpriteSheet sheet = new SpriteSheet(image, cellWidth, cellHeight);
        int needed = LAST_CHAR - FIRST_CHAR + 1;
        if (sheet.getCount() < needed) {
            throw new IllegalArgumentException("A font image needs " + needed + " cells (16 columns x 6 rows), "
                    + "but this one only has " + sheet.getCount());
        }
        this.cellHeight = cellHeight;

        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            Bitmap glyph = sheet.get(c - FIRST_CHAR);
            glyphs[c] = glyph;
            int inkWidth = measureInkWidth(glyph);
            // The "advance" is how far the cursor moves right after this letter. The
            // word comes from printing, where each letter was a small block of metal
            // and the "advance" was how far the line moved on.
            if (inkWidth == 0) {
                advances[c] = cellWidth / 2;    // a space, or an empty cell
            } else {
                advances[c] = inkWidth + 1;     // the letter, plus one pixel of space before the next
            }
        }
    }

    /** Loads a font image from the game's folder. */
    public static Font load(String path, int cellWidth, int cellHeight) {
        return new Font(Bitmap.load(path), cellWidth, cellHeight);
    }

    /**
     * The 8x8 font that comes with PGE. It is loaded the first time someone asks for it
     * and then kept in a static field, so every canvas and label shares the same one.
     * Waiting until something is needed before creating it is called "lazy loading".
     */
    public static Font getDefault() {
        if (defaultFont == null) {
            defaultFont = new Font(Bitmap.loadResource("font.png"), 8, 8);
        }
        return defaultFont;
    }

    /** The picture of one character. */
    public Bitmap getGlyph(char c) {
        return glyphs[toIndex(c)];
    }

    /** How far to move right after drawing this character. */
    public int getAdvance(char c) {
        return advances[toIndex(c)];
    }

    /**
     * Distance from one line of text to the next: the height of a letter cell plus one
     * pixel, so the lines do not touch. (Printers call this space "leading", after the
     * strips of lead they put between lines of metal letters.)
     */
    public int getLineHeight() {
        return cellHeight + 1;
    }

    /** How many pixels wide this text is (the widest line, if it has several). */
    public int getTextWidth(String text) {
        int widest = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineWidth = 0;
            } else if (c != '\r') {
                lineWidth = lineWidth + getAdvance(c);
                widest = Math.max(widest, lineWidth);
            }
        }
        if (widest > 0) {
            widest = widest - 1;    // no gap needed after the last letter
        }
        return widest;
    }

    /** How many pixels tall this text is. */
    public int getTextHeight(String text) {
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines = lines + 1;
            }
        }
        return lines * getLineHeight() - 1;
    }

    /**
     * The place of a character in the glyph arrays. A char can be used as a number in
     * Java, so 'A' is simply 65. Characters the font does not have get the box.
     */
    private static int toIndex(char c) {
        if (c < FIRST_CHAR || c > LAST_CHAR) {
            return MISSING_CHAR;
        }
        return c;
    }

    /**
     * How wide a letter really is: one more than the column of its rightmost visible
     * pixel, or 0 if the glyph is empty. We search the columns from the right and stop
     * at the first one that has any visible pixel in it.
     */
    private static int measureInkWidth(Bitmap glyph) {
        for (int x = glyph.getWidth() - 1; x >= 0; x--) {
            for (int y = 0; y < glyph.getHeight(); y++) {
                if (!Color.isTransparent(glyph.getPixel(x, y))) {
                    return x + 1;
                }
            }
        }
        return 0;
    }
}
