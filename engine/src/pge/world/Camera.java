package pge.world;

import pge.gfx.Canvas;

/**
 * Decides which part of the world is on screen.
 *
 * The camera has a position: the world point shown at the top-left corner of the
 * screen. Move it in update(), then point the canvas at it in draw():
 *
 *   // init()
 *   camera = new Camera(Engine.getWidth(), Engine.getHeight());
 *   camera.setLimits(map.getWidth(), map.getHeight());
 *
 *   // update()
 *   camera.follow(heroX, heroY);
 *
 *   // draw()
 *   camera.applyTo(canvas);
 *   ...draw the world...
 *   canvas.resetCamera();
 *   ...draw the score...
 *
 * LIMITS
 * With setLimits, the camera never shows anything outside the world: near an edge
 * it stops, and the hero walks off-center instead. If the world is smaller than the
 * screen, it is centered.
 *
 * SMOOTHNESS
 * By default follow() jumps straight to its target. setSmoothness(0.1) makes it
 * glide there instead, closing 10% of the distance every update. Lower is softer.
 */
public class Camera {

    private final int viewWidth;
    private final int viewHeight;

    private double x = 0;
    private double y = 0;

    private boolean limited = false;
    private int worldWidth;
    private int worldHeight;

    private double smoothness = 1.0;

    /** A camera for a screen of this size (usually Engine.getWidth() and Engine.getHeight()). */
    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    // ------------------------------------------------------------------ moving

    /** Moves toward having world point (targetX, targetY) in the middle of the screen. */
    public void follow(double targetX, double targetY) {
        double wantedX = targetX - viewWidth / 2.0;
        double wantedY = targetY - viewHeight / 2.0;
        x = x + (wantedX - x) * smoothness;
        y = y + (wantedY - y) * smoothness;
        keepInsideLimits();
    }

    /** Puts world point (targetX, targetY) in the middle of the screen right away, ignoring smoothness. */
    public void centerOn(double targetX, double targetY) {
        x = targetX - viewWidth / 2.0;
        y = targetY - viewHeight / 2.0;
        keepInsideLimits();
    }

    /** Puts world point (newX, newY) at the top-left corner of the screen. */
    public void moveTo(double newX, double newY) {
        x = newX;
        y = newY;
        keepInsideLimits();
    }

    // ------------------------------------------------------------------ settings

    /** Never show anything outside a world of this size (in pixels, starting at 0, 0). */
    public void setLimits(int worldWidth, int worldHeight) {
        this.limited = true;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        keepInsideLimits();
    }

    /** Let the camera go anywhere again. */
    public void removeLimits() {
        limited = false;
    }

    /** 1.0 follows instantly (the default). Smaller values, like 0.1, glide softly. */
    public void setSmoothness(double smoothness) {
        if (smoothness <= 0 || smoothness > 1) {
            throw new IllegalArgumentException("Smoothness must be more than 0 and at most 1, got " + smoothness);
        }
        this.smoothness = smoothness;
    }

    public double getSmoothness() {
        return smoothness;
    }

    // ------------------------------------------------------------------ using it

    /** Tells the canvas to draw from this camera's point of view. Call it in draw(). */
    public void applyTo(Canvas canvas) {
        canvas.setCamera(getX(), getY());
    }

    /** World x shown at the left edge of the screen. */
    public int getX() {
        return (int) Math.floor(x);
    }

    /** World y shown at the top edge of the screen. */
    public int getY() {
        return (int) Math.floor(y);
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    /** Converts an x on the screen (like the mouse position) to an x in the world. */
    public int toWorldX(int screenX) {
        return screenX + getX();
    }

    /** Converts a y on the screen to a y in the world. */
    public int toWorldY(int screenY) {
        return screenY + getY();
    }

    /** True if any part of this world rectangle is on screen. Useful to skip work for things far away. */
    public boolean canSee(int worldX, int worldY, int width, int height) {
        return worldX + width > getX() && worldX < getX() + viewWidth
                && worldY + height > getY() && worldY < getY() + viewHeight;
    }

    private void keepInsideLimits() {
        if (!limited) {
            return;
        }
        x = limit(x, worldWidth, viewWidth);
        y = limit(y, worldHeight, viewHeight);
    }

    /** Keeps one coordinate inside the world, or centers it when the world is smaller than the view. */
    private static double limit(double position, int worldSize, int viewSize) {
        if (worldSize <= viewSize) {
            return (worldSize - viewSize) / 2.0;
        }
        if (position < 0) {
            return 0;
        }
        if (position > worldSize - viewSize) {
            return worldSize - viewSize;
        }
        return position;
    }
}
