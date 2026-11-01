package pge.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * A rectangle of pixels: a sprite, a tile, a background, one letter of the font...
 *
 * The name comes from early computer screens, which could only show black or white.
 * One bit was enough for each pixel, so a picture was literally a "map of bits".
 * Today the word is used for any grid of pixels, whatever the colours.
 *
 * Like the Canvas, a bitmap is an int[] array of colours, stored row by row, so the
 * pixel at (x, y) is at index  y * width + x.  Any width and height are fine.
 *
 * Load one from a PNG file:
 *
 *   Bitmap hero = Bitmap.load("assets/hero.png");
 *
 * PNG ("Portable Network Graphics") was created in 1996 as a free replacement for the
 * GIF format, whose compression method was patented at the time. It is "lossless",
 * which means it stores every pixel exactly. That matters for pixel art, where every
 * single pixel was placed on purpose. (JPEG photos lose a little detail to save space,
 * which would smudge pixel art.)
 *
 * TRANSPARENCY
 * Pixels that should not be drawn are stored as Color.TRANSPARENT. When a picture is
 * loaded:
 *   - pixels that are mostly see-through in the PNG become transparent. A PNG can
 *     store a fourth value for each pixel, "alpha", from 0 (invisible) to 255 (solid).
 *     The idea of an alpha channel came from the computer graphics researchers Ed
 *     Catmull and Alvy Ray Smith in the 1970s.
 *   - optionally, every pixel of one chosen "colour key" becomes transparent too.
 *     Old games, from before alpha channels were common, painted their sprites on a
 *     background of one colour that never appears in the art, often bright magenta,
 *     and skipped that colour when drawing. Television uses the same idea with blue
 *     and green screens:  Bitmap.load("assets/hero.png", 0xFF00FF)
 */
public class Bitmap {

    private final int width;
    private final int height;
    private final int[] pixels;

    /** A new bitmap of the given size, completely transparent. Paint it with setPixel. */
    public Bitmap(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Bitmap size must be positive, got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = Color.TRANSPARENT;
        }
    }

    // ------------------------------------------------------------------ loading

    /** Loads a PNG file. See-through pixels become transparent. */
    public static Bitmap load(String path) {
        return load(path, Color.TRANSPARENT);
    }

    /**
     * Loads a PNG file. See-through pixels, and pixels of exactly the colour colorKey,
     * become transparent. The path is relative to the folder the game was started in,
     * which Build.java sets to the game's own folder.
     */
    public static Bitmap load(String path, int colorKey) {
        File file = new File(path);
        if (!file.isFile()) {
            throw new RuntimeException("Could not find the image '" + path + "'. "
                    + "Looked for it here: " + file.getAbsolutePath());
        }
        BufferedImage image;
        try {
            // ImageIO is Java's library for reading and writing pictures (part of Java
            // since version 1.4). It understands PNG, JPEG, GIF and BMP.
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the image '" + path + "': " + e.getMessage(), e);
        }
        if (image == null) {
            // ImageIO returns null, instead of throwing an exception, when it does not
            // recognise the file format.
            throw new RuntimeException("'" + path + "' is not an image format Java understands. Save it as PNG.");
        }
        return fromImage(image, colorKey);
    }

    /**
     * Loads a picture that belongs to the engine itself, such as the default font.
     *
     * These files are not in the game's folder. They sit next to the engine's compiled
     * classes, in the same folder as the classes of this package (pge/gfx).
     * getResourceAsStream looks for a file there, along the "class path": the list of
     * places where Java looks for classes. That way the file is found both when the
     * game runs from the out/ folder and when it runs from inside a .jar file.
     */
    static Bitmap loadResource(String name) {
        InputStream stream = Bitmap.class.getResourceAsStream(name);
        if (stream == null) {
            throw new RuntimeException("The engine file '" + name + "' is missing. Was the engine built with Build.java?");
        }
        try {
            BufferedImage image = ImageIO.read(stream);
            stream.close();
            return fromImage(image, Color.TRANSPARENT);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the engine file '" + name + "': " + e.getMessage(), e);
        }
    }

    /** Copies a picture loaded by Java into our own array of colours. */
    private static Bitmap fromImage(BufferedImage image, int colorKey) {
        Bitmap bitmap = new Bitmap(image.getWidth(), image.getHeight());
        for (int y = 0; y < bitmap.height; y++) {
            for (int x = 0; x < bitmap.width; x++) {
                // getRGB gives the colour as 0xAARRGGBB: our 0xRRGGBB with the alpha
                // value (AA) in front, from 00 (invisible) to FF (solid). We split it
                // into the two parts with the bit operations explained in Color.java.
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int rgb = argb & 0xFFFFFF;
                // PGE pixels are either drawn or not, so we round: below 128 (more than
                // half see-through) counts as transparent.
                if (alpha < 128 || rgb == colorKey) {
                    bitmap.pixels[y * bitmap.width + x] = Color.TRANSPARENT;
                } else {
                    bitmap.pixels[y * bitmap.width + x] = rgb;
                }
            }
        }
        return bitmap;
    }

    // ------------------------------------------------------------------ pixels

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** The colour at (x, y), or Color.TRANSPARENT if (x, y) is outside the bitmap. */
    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Color.TRANSPARENT;
        }
        return pixels[y * width + x];
    }

    /** Changes the colour at (x, y). Does nothing if (x, y) is outside the bitmap. */
    public void setPixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        pixels[y * width + x] = color;
    }

    /**
     * A new bitmap holding a copy of one rectangle of this one. SpriteSheet uses it to
     * cut a sheet into separate pictures.
     */
    public Bitmap crop(int x, int y, int cropWidth, int cropHeight) {
        Bitmap piece = new Bitmap(cropWidth, cropHeight);
        for (int row = 0; row < cropHeight; row++) {
            for (int col = 0; col < cropWidth; col++) {
                piece.setPixel(col, row, getPixel(x + col, y + row));
            }
        }
        return piece;
    }
}
