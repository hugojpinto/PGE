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
 * it stops, and the hero walks off-centre instead. If the world is smaller than the
 * screen, it is centred.
 *
 * SMOOTHNESS
 * By default follow() jumps straight to its target. setSmoothness(0.1) makes it
 * glide there instead, closing 10% of the remaining distance every update. Lower is
 * softer.
 *
 * DEAD ZONE
 * setDeadZone(40, 120) gives the middle of the screen a 40x120 box in which the
 * target may move without the camera moving at all. The camera only follows once
 * the target pushes against an edge of the box. A tall dead zone stops the whole
 * world from bobbing up and down every time a platformer hero jumps.
 *
 * Side-scrolling games have used tricks like these since the 1980s; Super Mario Bros.
 * (1985), for example, only scrolls when Mario gets past the middle of the screen. If
 * you want to learn more, the game developer Itay Keren collected dozens of these
 * techniques from classic games in a well-known talk and article, "Scroll Back: The
 * Theory and Practice of Cameras in Side-Scrollers" (2015).
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
    private int deadZoneWidth = 0;
    private int deadZoneHeight = 0;

    /** A camera for a screen of this size (usually Engine.getWidth() and Engine.getHeight()). */
    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    // ------------------------------------------------------------------ moving

    /**
     * Moves toward having world point (targetX, targetY) in the middle of the screen,
     * or, with a dead zone, just inside the dead zone. Call it once in every update.
     *
     * The smoothing line, position + (wanted - position) * smoothness, moves the camera
     * a fixed fraction of the remaining distance. Far from the target, that is a big
     * step; close to it, a tiny one. So the camera starts quickly and slows down
     * gently as it arrives. Moving a fraction of the way from one value to another is
     * called "linear interpolation", or "lerp" for short, and doing it every update like
     * this is one of the most common tricks in game programming.
     */
    public void follow(double targetX, double targetY) {
        double wantedX = wantedPosition(x, targetX, viewWidth, deadZoneWidth);
        double wantedY = wantedPosition(y, targetY, viewHeight, deadZoneHeight);
        x = x + (wantedX - x) * smoothness;
        y = y + (wantedY - y) * smoothness;
        keepInsideLimits();
    }

    /**
     * Where the camera should be along one axis (x or y). The dead zone is centred on
     * the screen and reaches deadZoneSize / 2 to each side of the middle. If the target
     * is inside it, stay put. If it is outside, move just enough to bring the target
     * back to the edge of the dead zone. With no dead zone (size 0), that means "put
     * the target exactly in the middle".
     *
     * This method is "static" because it only uses its parameters, not the fields of
     * a camera, and it is called once for x and once for y.
     */
    private static double wantedPosition(double position, double target, int viewSize, int deadZoneSize) {
        double middle = position + viewSize / 2.0;
        double halfZone = deadZoneSize / 2.0;
        if (target < middle - halfZone) {
            return target + halfZone - viewSize / 2.0;
        }
        if (target > middle + halfZone) {
            return target - halfZone - viewSize / 2.0;
        }
        return position;
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

    /** A box in the middle of the screen where the target can move without moving the camera. 0, 0 for none. */
    public void setDeadZone(int width, int height) {
        if (width < 0 || height < 0 || width > viewWidth || height > viewHeight) {
            throw new IllegalArgumentException("The dead zone must fit on the screen, got " + width + "x" + height);
        }
        deadZoneWidth = width;
        deadZoneHeight = height;
    }

    public int getDeadZoneWidth() {
        return deadZoneWidth;
    }

    public int getDeadZoneHeight() {
        return deadZoneHeight;
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
