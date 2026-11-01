package pge.ui;

import pge.gfx.Canvas;

/**
 * Something drawn on the screen, on top of the game: a box, a label, a button...
 *
 * UI elements use screen coordinates: (0, 0) is the top-left corner of the screen,
 * and the camera does not move them. Add them to a scene with addUI(...), and the
 * scene updates and draws them for you.
 *
 * This class is "abstract": it is not a complete element by itself, only the part
 * all elements share (a position, a size, being visible or not). Every kind of
 * element must say how it is drawn by writing its own draw() method.
 */
public abstract class UIElement {

    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible = true;

    protected UIElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Called every update while visible. Empty here; buttons use it to check for clicks. */
    public void update() {
    }

    /** Draws the element. Every kind of element writes its own. */
    public abstract void draw(Canvas canvas);

    /** True if the screen point (px, py) is inside this element. */
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public final void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    /** Hidden elements are neither drawn nor updated (a hidden button cannot be clicked). */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
