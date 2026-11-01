package pge.ui;

import pge.gfx.Canvas;

/**
 * Something drawn on the screen, on top of the game: a box, a label, a button...
 * "UI" stands for "user interface", the parts of a program the user looks at and
 * clicks. In games, the score, lives and timer shown on top of the action are also
 * called the "HUD", for "heads-up display", a name borrowed from the displays that
 * show information on the cockpit window of a fighter plane.
 *
 * UI elements use screen positions: (0, 0) is the top-left corner of the screen, and
 * the camera does not move them. Add them to a scene with addUI(...), and the scene
 * updates and draws them for you.
 *
 * This class is "abstract": it is not a complete element by itself, only the part
 * that all elements share (a position, a size, and whether it is visible). You cannot
 * write new UIElement(...). Every kind of element extends it and must write its own
 * draw() method, which is why draw() is marked abstract too. The scene can then keep
 * all elements in one list of UIElements and call draw() on each, and Java runs the
 * right version for each kind of element.
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

    /** Hidden elements are neither drawn nor updated, so a hidden button cannot be clicked. */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
