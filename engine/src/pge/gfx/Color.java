package pge.gfx;

/**
 * Colors in PGE are plain ints written as 0xRRGGBB.
 *
 * Each pair of hexadecimal digits is one channel, from 00 (none) to FF (full):
 *
 *   0xFF0000  red      0x00FF00  green     0x0000FF  blue
 *   0xFFFFFF  white    0x000000  black     0xFFFF00  yellow (red + green)
 *
 * You can write colors directly as numbers, use the named constants below,
 * or build them with Color.rgb(red, green, blue).
 *
 * TRANSPARENT is a special value meaning "draw nothing here". It is used for
 * the see-through parts of sprites.
 */
public final class Color {

    /** "Draw nothing". Not a real color: -1 is outside the 0x000000-0xFFFFFF range. */
    public static final int TRANSPARENT = -1;

    // The PGE default palette. Use any other color you like, these are just handy names.
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

    /** Nobody needs a Color object: everything here is static. */
    private Color() {
    }

    /**
     * Builds a color from its red, green and blue parts, each from 0 to 255.
     * Values outside that range are clamped, so rgb(300, -5, 0) is the same as rgb(255, 0, 0).
     */
    public static int rgb(int red, int green, int blue) {
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
        // Shift each channel into its place: 0xRR0000 + 0x00GG00 + 0x0000BB
        return (red << 16) | (green << 8) | blue;
    }

    /** The red part (0-255) of a color. */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /** The green part (0-255) of a color. */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /** The blue part (0-255) of a color. */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /** True if this "color" means "draw nothing". */
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
