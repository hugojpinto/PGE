package pge.gfx;

import java.util.Arrays;

/**
 * The canvas is the picture the player sees, stored as one long array of pixels.
 * (Graphics programmers call an array like this a "frame buffer".)
 *
 * A 640x360 canvas holds 640 * 360 = 230,400 ints, one colour per pixel, row by row:
 *
 *   index 0 .. 639        is the top row (y = 0)
 *   index 640 .. 1279     is the second row (y = 1)
 *   ...
 *
 * So the pixel at (x, y) is at index  y * width + x.  Storing a grid row by row like
 * this is called "row-major order". Screens have stored their pixels this way since
 * the early days of computer graphics, because a screen is also drawn row by row,
 * from the top.
 *
 * Every drawing method in this class is a careful way of writing colours into that
 * array. Nothing else is involved: once the frame is finished, Window shows the array
 * on the screen.
 *
 * CAMERA
 * Games often have a world bigger than the screen. setCamera(x, y) says which part of
 * the world is visible: everything you draw is moved left by x and up by y. Draw the
 * world with the camera set, then call resetCamera() and draw the score, the menus and
 * anything else that should stay in the same place on the screen.
 *
 * CLIPPING
 * You may draw partly or completely outside the canvas. Pixels that would land outside
 * are skipped, so you never need to check the edges yourself. Skipping the parts of a
 * drawing that fall outside the visible area is called "clipping".
 *
 * Every public method below follows the same three steps: subtract the camera
 * position, skip what is off the canvas, write the colours into the array.
 */
public class Canvas {

    private final int width;
    private final int height;
    private final int[] pixels;

    private int cameraX = 0;
    private int cameraY = 0;

    private Font font = null;    // null means "the default font", which is loaded when first needed

    /**
     * Wraps an existing array of pixels. The engine creates the canvas for you; you
     * only need this constructor if you want an extra canvas that is not shown, for
     * example to draw into a picture and save it.
     */
    public Canvas(int width, int height, int[] pixels) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Canvas size must be positive, got " + width + "x" + height);
        }
        if (pixels.length != width * height) {
            throw new IllegalArgumentException("A " + width + "x" + height + " canvas needs "
                    + (width * height) + " pixels, but the array has " + pixels.length);
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ------------------------------------------------------------------ camera

    /** From now on, the world position (x, y) is drawn at the top-left corner of the screen. */
    public void setCamera(int x, int y) {
        cameraX = x;
        cameraY = y;
    }

    /** Back to screen positions: (0, 0) is the top-left corner of the screen. */
    public void resetCamera() {
        cameraX = 0;
        cameraY = 0;
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    // ------------------------------------------------------------------ drawing

    /**
     * Paints the whole canvas with one colour. The camera does not matter here.
     * Arrays.fill sets every element of an array to the same value, much faster than
     * a loop we would write ourselves.
     */
    public void clear(int color) {
        Arrays.fill(pixels, color);
    }

    /** Paints a single pixel. */
    public void setPixel(int x, int y, int color) {
        plot(x - cameraX, y - cameraY, color);
    }

    /** Reads the colour of a single pixel, or Color.TRANSPARENT if (x, y) is off the canvas. */
    public int getPixel(int x, int y) {
        int screenX = x - cameraX;
        int screenY = y - cameraY;
        if (screenX < 0 || screenX >= width || screenY < 0 || screenY >= height) {
            return Color.TRANSPARENT;
        }
        return pixels[screenY * width + screenX];
    }

    /** A filled rectangle. (x, y) is its top-left corner. It is drawn one row at a time. */
    public void fillRect(int x, int y, int rectWidth, int rectHeight, int color) {
        if (Color.isTransparent(color) || rectWidth <= 0 || rectHeight <= 0) {
            return;
        }
        int left = x - cameraX;
        int top = y - cameraY;
        for (int row = top; row < top + rectHeight; row++) {
            fillSpan(left, left + rectWidth - 1, row, color);
        }
    }

    /** The 1-pixel outline of a rectangle, made of four thin filled rectangles. (x, y) is its top-left corner. */
    public void drawRect(int x, int y, int rectWidth, int rectHeight, int color) {
        if (rectWidth <= 0 || rectHeight <= 0) {
            return;
        }
        int right = x + rectWidth - 1;
        int bottom = y + rectHeight - 1;
        fillRect(x, y, rectWidth, 1, color);          // top edge
        fillRect(x, bottom, rectWidth, 1, color);     // bottom edge
        fillRect(x, y, 1, rectHeight, color);         // left edge
        fillRect(right, y, 1, rectHeight, color);     // right edge
    }

    /**
     * A straight line from (x1, y1) to (x2, y2), with both ends included.
     *
     * A screen is a grid, so a sloping line has to be drawn as a staircase of pixels.
     * The question is which pixels to pick. This method uses Bresenham's line
     * algorithm, which Jack Bresenham worked out at IBM in 1962 to drive a pen
     * plotter (a machine that draws with a pen on paper), and published in 1965. It is
     * still the standard way to draw lines on a grid.
     *
     * The idea: walk from one end of the line to the other, one pixel at a time. Keep a
     * running number, "error", that measures how far the pixels drawn so far are from
     * the true line. At each step, the error says whether to move sideways, up or down,
     * or both, to stay as close to the line as possible. The clever part is that it
     * only ever adds and compares whole numbers. Computers in 1962 were very slow at
     * fractions and division, so this made it fast, and it still is.
     */
    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        int x = x1 - cameraX;
        int y = y1 - cameraY;
        int endX = x2 - cameraX;
        int endY = y2 - cameraY;

        int distanceX = Math.abs(endX - x);
        int distanceY = -Math.abs(endY - y);    // negative on purpose: it makes the error checks below simpler
        int stepX = 1;
        if (x > endX) {
            stepX = -1;
        }
        int stepY = 1;
        if (y > endY) {
            stepY = -1;
        }
        int error = distanceX + distanceY;

        while (true) {
            plot(x, y, color);
            if (x == endX && y == endY) {
                break;
            }
            int doubleError = 2 * error;
            if (doubleError >= distanceY) {    // the line has moved far enough sideways: step in x
                error = error + distanceY;
                x = x + stepX;
            }
            if (doubleError <= distanceX) {    // the line has moved far enough up or down: step in y
                error = error + distanceX;
                y = y + stepY;
            }
        }
    }

    /**
     * The outline of a circle with its centre at (centerX, centerY).
     *
     * This uses the "midpoint circle algorithm", which applies Bresenham's idea to
     * circles. A circle looks the same in all eight "octants" (slices of 45 degrees),
     * so we only work out one eighth of it and mirror each point into the other
     * seven: (x, y), (-x, y), (x, -y), (y, x) and so on.
     *
     * We start at the right edge of the circle, at (radius, 0), and go up one row at a
     * time. At each row, the next pixel is either straight up or up and one step left.
     * "error" tells us which one: it says whether the point halfway between the two
     * choices (the "midpoint") is inside or outside the circle. As before, only whole
     * numbers are used.
     */
    public void drawCircle(int centerX, int centerY, int radius, int color) {
        if (radius < 0) {
            return;
        }
        int cx = centerX - cameraX;
        int cy = centerY - cameraY;
        int x = radius;
        int y = 0;
        int error = 1 - radius;

        while (x >= y) {    // stop when the eighth is done, where x and y meet at 45 degrees
            plot(cx + x, cy + y, color);
            plot(cx - x, cy + y, color);
            plot(cx + x, cy - y, color);
            plot(cx - x, cy - y, color);
            plot(cx + y, cy + x, color);
            plot(cx - y, cy + x, color);
            plot(cx + y, cy - x, color);
            plot(cx - y, cy - x, color);

            y = y + 1;
            if (error < 0) {
                // The midpoint is inside the circle: keep the same x.
                error = error + 2 * y + 1;
            } else {
                // The midpoint is outside the circle: step one pixel in.
                x = x - 1;
                error = error + 2 * (y - x) + 1;
            }
        }
    }

    /**
     * A filled circle with its centre at (centerX, centerY). It uses the same
     * algorithm as drawCircle, but instead of plotting the mirrored points, it fills
     * the row between each pair of them.
     */
    public void fillCircle(int centerX, int centerY, int radius, int color) {
        if (radius < 0 || Color.isTransparent(color)) {
            return;
        }
        int cx = centerX - cameraX;
        int cy = centerY - cameraY;
        int x = radius;
        int y = 0;
        int error = 1 - radius;

        while (x >= y) {
            fillSpan(cx - x, cx + x, cy + y, color);
            fillSpan(cx - x, cx + x, cy - y, color);
            fillSpan(cx - y, cx + y, cy + x, color);
            fillSpan(cx - y, cx + y, cy - x, color);

            y = y + 1;
            if (error < 0) {
                error = error + 2 * y + 1;
            } else {
                x = x - 1;
                error = error + 2 * (y - x) + 1;
            }
        }
    }

    // ------------------------------------------------------------------ bitmaps

    /** Draws a bitmap with its top-left corner at (x, y). Transparent pixels are skipped. */
    public void drawBitmap(Bitmap bitmap, int x, int y) {
        drawBitmap(bitmap, x, y, false, false);
    }

    /**
     * Draws a bitmap, and can mirror it. flipX mirrors it left to right (for a
     * character turning around), flipY mirrors it upside down.
     *
     * The two loops go through every pixel of the bitmap. For each one we work out
     * where it lands on the canvas. Pixels that land off the canvas, and transparent
     * pixels, are skipped. To mirror the picture, we read it from the other end: the
     * last column instead of the first, and so on. Old game consoles did the same in
     * hardware, so one picture of a character could face both ways and save memory.
     */
    public void drawBitmap(Bitmap bitmap, int x, int y, boolean flipX, boolean flipY) {
        if (bitmap == null) {
            throw new IllegalArgumentException("drawBitmap was given null instead of a bitmap. Was it loaded?");
        }
        int left = x - cameraX;
        int top = y - cameraY;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        for (int row = 0; row < bitmapHeight; row++) {
            int screenY = top + row;
            if (screenY < 0 || screenY >= height) {
                continue;    // this whole row is off the canvas
            }
            int sourceRow = row;
            if (flipY) {
                sourceRow = bitmapHeight - 1 - row;
            }
            for (int col = 0; col < bitmapWidth; col++) {
                int screenX = left + col;
                if (screenX < 0 || screenX >= width) {
                    continue;    // this pixel is off the canvas
                }
                int sourceCol = col;
                if (flipX) {
                    sourceCol = bitmapWidth - 1 - col;
                }
                int color = bitmap.getPixel(sourceCol, sourceRow);
                if (!Color.isTransparent(color)) {
                    pixels[screenY * width + screenX] = color;
                }
            }
        }
    }

    /**
     * Draws only the shape of a bitmap: every visible pixel in the same colour.
     * Games use this to make a character flash white when it gets hit, or to draw a
     * dark shadow under it.
     */
    public void drawSilhouette(Bitmap bitmap, int x, int y, boolean flipX, int color) {
        if (bitmap == null) {
            throw new IllegalArgumentException("drawSilhouette was given null instead of a bitmap. Was it loaded?");
        }
        for (int row = 0; row < bitmap.getHeight(); row++) {
            for (int col = 0; col < bitmap.getWidth(); col++) {
                int sourceCol = col;
                if (flipX) {
                    sourceCol = bitmap.getWidth() - 1 - col;
                }
                if (!Color.isTransparent(bitmap.getPixel(sourceCol, row))) {
                    setPixel(x + col, y + row, color);
                }
            }
        }
    }

    // ------------------------------------------------------------------ text

    /** Writes text with its top-left corner at (x, y). "\n" starts a new line. */
    public void drawText(String text, int x, int y, int color) {
        drawText(text, x, y, color, 1);
    }

    /**
     * Writes text, made bigger: scale 2 turns every pixel of every letter into a 2x2
     * square, scale 3 into a 3x3 square, and so on. Good for titles.
     *
     * Each letter is a small picture in the font (a "glyph"). We draw it, then move
     * the "cursor" right by that letter's width, the way a typewriter moves its paper
     * along after each key.
     */
    public void drawText(String text, int x, int y, int color, int scale) {
        Font currentFont = getFont();
        int cursorX = x;
        int cursorY = y;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r') {
                // Windows ends each line of a text file with two characters, '\r' and
                // '\n'; macOS and Linux use only '\n'. The '\n' does the work, so we
                // skip '\r'. (The names come from typewriters: "carriage return" moved
                // back to the start of the line, "line feed" moved the paper up.)
                continue;
            }
            if (c == '\n') {
                cursorX = x;
                cursorY = cursorY + currentFont.getLineHeight() * scale;
                continue;
            }
            Bitmap glyph = currentFont.getGlyph(c);
            for (int row = 0; row < glyph.getHeight(); row++) {
                for (int col = 0; col < glyph.getWidth(); col++) {
                    if (!Color.isTransparent(glyph.getPixel(col, row))) {
                        fillRect(cursorX + col * scale, cursorY + row * scale, scale, scale, color);
                    }
                }
            }
            cursorX = cursorX + currentFont.getAdvance(c) * scale;
        }
    }

    /** How many pixels wide this text would be. Useful for centring it. */
    public int getTextWidth(String text) {
        return getFont().getTextWidth(text);
    }

    /** How many pixels wide this text would be at this scale. */
    public int getTextWidth(String text, int scale) {
        return getFont().getTextWidth(text) * scale;
    }

    /** How many pixels tall this text would be. */
    public int getTextHeight(String text) {
        return getFont().getTextHeight(text);
    }

    /** The font used by drawText. */
    public Font getFont() {
        if (font == null) {
            font = Font.getDefault();
        }
        return font;
    }

    /** Changes the font used by drawText from now on. */
    public void setFont(Font font) {
        this.font = font;
    }

    // ------------------------------------------------------------------ helpers
    // These two methods work in SCREEN positions: the camera has already been subtracted.

    /** Writes one pixel, if it is on the canvas and the colour is not transparent. */
    private void plot(int screenX, int screenY, int color) {
        if (screenX < 0 || screenX >= width || screenY < 0 || screenY >= height) {
            return;
        }
        if (Color.isTransparent(color)) {
            return;
        }
        pixels[screenY * width + screenX] = color;
    }

    /**
     * Fills part of one row, from left to right (both included), cut to the edges of
     * the canvas. Because a row is stored as one stretch of the array, the whole span
     * can be filled with a single Arrays.fill call.
     */
    private void fillSpan(int left, int right, int screenY, int color) {
        if (screenY < 0 || screenY >= height) {
            return;
        }
        if (left < 0) {
            left = 0;
        }
        if (right >= width) {
            right = width - 1;
        }
        if (left > right) {
            return;
        }
        int rowStart = screenY * width;
        Arrays.fill(pixels, rowStart + left, rowStart + right + 1, color);
    }
}
