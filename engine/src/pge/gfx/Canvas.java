package pge.gfx;

import java.util.Arrays;

/**
 * The canvas is the picture the player sees, stored as one long array of pixels.
 *
 * A 640x360 canvas holds 640 * 360 = 230,400 ints, one color per pixel, row by row:
 *
 *   index 0 .. 639        is the top row (y = 0)
 *   index 640 .. 1279     is the second row (y = 1)
 *   ...
 *
 * So the pixel at (x, y) lives at index  y * width + x.
 * Every drawing method below is just a careful way of writing colors into that array.
 *
 * CAMERA
 * Games often have a world bigger than the screen. setCamera(x, y) says which part of
 * the world is visible: everything you draw is shifted left by x and up by y.
 * Draw the world with the camera set, then call resetCamera() and draw the score,
 * menus and other things that should stay fixed on the screen.
 *
 * CLIPPING
 * You may draw partly or fully outside the canvas. Pixels that fall outside are
 * simply skipped, so you never need to check the edges yourself.
 */
public class Canvas {

    private final int width;
    private final int height;
    private final int[] pixels;

    private int cameraX = 0;
    private int cameraY = 0;

    /**
     * Wraps an existing pixel array. The engine creates the canvas for you;
     * you only need this constructor if you want an extra off-screen canvas.
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

    /** From now on, (x, y) in the world appears at the top-left corner of the screen. */
    public void setCamera(int x, int y) {
        cameraX = x;
        cameraY = y;
    }

    /** Back to screen coordinates: (0, 0) is the top-left corner of the screen. */
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

    /** Paints the whole canvas with one color. The camera does not matter here. */
    public void clear(int color) {
        Arrays.fill(pixels, color);
    }

    /** Paints a single pixel. */
    public void setPixel(int x, int y, int color) {
        plot(x - cameraX, y - cameraY, color);
    }

    /** Reads the color of a single pixel, or Color.TRANSPARENT if (x, y) is off the canvas. */
    public int getPixel(int x, int y) {
        int screenX = x - cameraX;
        int screenY = y - cameraY;
        if (screenX < 0 || screenX >= width || screenY < 0 || screenY >= height) {
            return Color.TRANSPARENT;
        }
        return pixels[screenY * width + screenX];
    }

    /** A solid rectangle. (x, y) is its top-left corner. */
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

    /** The 1-pixel outline of a rectangle. (x, y) is its top-left corner. */
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
     * A straight line from (x1, y1) to (x2, y2), both ends included.
     *
     * Uses Bresenham's line algorithm (1962): walk from one end to the other one pixel
     * at a time, and keep a running "error" that says when it is time to also step
     * sideways. It needs only whole numbers, no fractions.
     */
    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        int x = x1 - cameraX;
        int y = y1 - cameraY;
        int endX = x2 - cameraX;
        int endY = y2 - cameraY;

        int distanceX = Math.abs(endX - x);
        int distanceY = -Math.abs(endY - y);
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
            if (doubleError >= distanceY) {
                error = error + distanceY;
                x = x + stepX;
            }
            if (doubleError <= distanceX) {
                error = error + distanceX;
                y = y + stepY;
            }
        }
    }

    /**
     * The outline of a circle centered on (centerX, centerY).
     *
     * Uses the midpoint circle algorithm: compute one eighth of the circle and
     * mirror it eight ways, because a circle looks the same in every octant.
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

        while (x >= y) {
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
                error = error + 2 * y + 1;
            } else {
                x = x - 1;
                error = error + 2 * (y - x) + 1;
            }
        }
    }

    /** A solid circle centered on (centerX, centerY). Same algorithm, but filling rows. */
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

    // ------------------------------------------------------------------ helpers
    // These work in SCREEN coordinates: the camera has already been applied.

    /** Writes one pixel, if it is on the canvas and the color is not transparent. */
    private void plot(int screenX, int screenY, int color) {
        if (screenX < 0 || screenX >= width || screenY < 0 || screenY >= height) {
            return;
        }
        if (Color.isTransparent(color)) {
            return;
        }
        pixels[screenY * width + screenX] = color;
    }

    /** Fills one row from left to right (both included), cut to the edges of the canvas. */
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
