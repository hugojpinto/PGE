package pge.gfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * A rectangle of pixels: a sprite, a tile, a background, a letter of the font...
 *
 * Like the Canvas, a bitmap is just an int[] array of colors, row by row, so the
 * pixel at (x, y) lives at index  y * width + x.  Any width and height are fine.
 *
 * Load one from a PNG file:
 *
 *   Bitmap hero = Bitmap.load("assets/hero.png");
 *
 * TRANSPARENCY
 * Pixels that should not be drawn are stored as Color.TRANSPARENT. When loading:
 *   - pixels that are (mostly) see-through in the PNG become transparent, and
 *   - optionally, every pixel of one chosen "color key" does too. Old games often
 *     used bright magenta for this:  Bitmap.load("assets/hero.png", 0xFF00FF)
 */
public class Bitmap {

    private final int width;
    private final int height;
    private final int[] pixels;

    /** A new bitmap of the given size, fully transparent. Paint it with setPixel. */
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

    /** Loads a PNG file. See-through pixels, and pixels of exactly colorKey, become transparent. */
    public static Bitmap load(String path, int colorKey) {
        File file = new File(path);
        if (!file.isFile()) {
            throw new RuntimeException("Could not find the image '" + path + "'. "
                    + "Looked for it here: " + file.getAbsolutePath());
        }
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the image '" + path + "': " + e.getMessage(), e);
        }
        if (image == null) {
            throw new RuntimeException("'" + path + "' is not an image format Java understands. Save it as PNG.");
        }
        return fromImage(image, colorKey);
    }

    /**
     * Loads an image that is packaged with the engine itself (like the default font).
     * It is looked up next to the compiled engine classes of this package (pge.gfx),
     * not in the game's folder.
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

    /** Copies a JDK image into our own pixel format. */
    private static Bitmap fromImage(BufferedImage image, int colorKey) {
        Bitmap bitmap = new Bitmap(image.getWidth(), image.getHeight());
        for (int y = 0; y < bitmap.height; y++) {
            for (int x = 0; x < bitmap.width; x++) {
                // getRGB gives 0xAARRGGBB: AA is the opacity, from 00 (invisible) to FF (solid).
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int rgb = argb & 0xFFFFFF;
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

    /** The color at (x, y), or Color.TRANSPARENT if (x, y) is outside the bitmap. */
    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Color.TRANSPARENT;
        }
        return pixels[y * width + x];
    }

    /** Changes the color at (x, y). Does nothing if (x, y) is outside the bitmap. */
    public void setPixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        pixels[y * width + x] = color;
    }

    /** A new bitmap holding a copy of one rectangle of this one. Used to cut up sprite sheets. */
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
