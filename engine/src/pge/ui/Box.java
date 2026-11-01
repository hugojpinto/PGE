package pge.ui;

import pge.gfx.Canvas;
import pge.gfx.Color;

/**
 * A filled rectangle with a border: a panel to put a score, a dialog or a menu on.
 *
 *   Box panel = new Box(10, 10, 200, 80);
 *   panel.setFillColor(Color.DARK_BLUE);
 *
 * Use Color.TRANSPARENT as the fill colour for a frame only, or as the border colour
 * for no border. (Drawing methods skip the colour TRANSPARENT; see Color.java.)
 */
public class Box extends UIElement {

    private int fillColor = Color.BLACK;
    private int borderColor = Color.WHITE;

    public Box(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.fillRect(getX(), getY(), getWidth(), getHeight(), fillColor);
        canvas.drawRect(getX(), getY(), getWidth(), getHeight(), borderColor);
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int color) {
        fillColor = color;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int color) {
        borderColor = color;
    }
}
