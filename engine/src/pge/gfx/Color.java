package pge.gfx;

/**
 * Colours in PGE are plain ints, written in hexadecimal as 0xRRGGBB.
 *
 * Screens make every colour by mixing red, green and blue light. Each pair of
 * hexadecimal digits says how much of one of them to use, from 00 (none) to FF
 * (full, which is 255 in decimal):
 *
 *   0xFF0000  red      0x00FF00  green     0x0000FF  blue
 *   0xFFFFFF  white    0x000000  black     0xFFFF00  yellow (red light + green light)
 *
 * Mixing light works differently from mixing paint: red and green light make yellow,
 * and all three together make white. With 256 levels for each of the three, there are
 * 256 * 256 * 256 = about 16.7 million colours. This 0xRRGGBB notation is the same one
 * web pages use, where it is written #RRGGBB, so you can copy colours from any web
 * colour picker.
 *
 * You can write colours directly as numbers, use the named constants below, or build
 * them with Color.rgb(red, green, blue).
 *
 * TRANSPARENT is a special value that means "draw nothing here". It is used for the
 * see-through parts of sprites.
 */
public final class Color {

    /**
     * "Draw nothing". This is not a real colour: real colours go from 0x000000 to
     * 0xFFFFFF, which are all positive numbers, so -1 can never be confused with one.
     */
    public static final int TRANSPARENT = -1;

    // The PGE palette: a small set of colours that look good together. Old game
    // machines could only show a few colours at once, so their games were designed
    // around a fixed palette, and many pixel artists still work that way (Pico-8, for
    // example, has 16). You can use any other colour too; these are just handy names.
    public static final int BLACK      = 0x000000;
    public static final int DARK_GRAY  = 0x3B3B4F;
    public static final int GRAY       = 0x8B8B9E;
    public static final int LIGHT_GRAY = 0xC9C9D6;
    public static final int WHITE      = 0xFFFFFF;
    public static final int DARK_RED   = 0x8E2236;
    public static final int RED        = 0xE0403F;
    public static final int ORANGE     = 0xF28C28;
    public static final int YELLOW     = 0xF7D83B;
    public static final int BROWN      = 0x8C5A34;
    public static final int DARK_GREEN = 0x1F6B3D;
    public static final int GREEN      = 0x4CC04A;
    public static final int DARK_BLUE  = 0x1B1C3A;
    public static final int BLUE       = 0x3C78E0;
    public static final int CYAN       = 0x45C8E0;
    public static final int PURPLE     = 0x7E4AC2;
    public static final int PINK       = 0xF283B4;

    /**
     * Nobody needs a Color object: a colour is just an int, and everything here is
     * static. Making the constructor private stops anyone from writing new Color().
     */
    private Color() {
    }

    /**
     * Builds a colour from its red, green and blue parts, each from 0 to 255.
     * Values outside that range are clamped (cut to the nearest allowed value), so
     * rgb(300, -5, 0) is the same as rgb(255, 0, 0).
     */
    public static int rgb(int red, int green, int blue) {
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
        // An int has 32 bits. Each part needs 8 bits (0 to 255), and the three parts sit
        // side by side: red in bits 16 to 23, green in bits 8 to 15, blue in bits 0 to 7.
        // "<<" shifts a number's bits to the left: red << 16 is red * 65536, which puts
        // it in the RR position. "|" (bitwise or) then combines the three parts.
        return (red << 16) | (green << 8) | blue;
    }

    /**
     * The red part (0 to 255) of a colour. ">>" shifts the bits back to the right, and
     * "& 0xFF" keeps only the lowest 8 bits, throwing away the rest. Keeping some bits
     * and clearing others like this is called "masking".
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /** The green part (0 to 255) of a colour. */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /** The blue part (0 to 255) of a colour. */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /** True if this "colour" means "draw nothing". */
    public static boolean isTransparent(int color) {
        return color == TRANSPARENT;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }
}
