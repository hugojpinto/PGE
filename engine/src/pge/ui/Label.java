package pge.ui;

import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.gfx.Font;

/**
 * A line (or a few lines) of text on the screen.
 *
 *   Label score = new Label("SCORE 0", 4, 4);
 *   addUI(score);
 *   ...
 *   score.setText("SCORE " + points);
 *
 * Options: setColor, setScale (2 = twice as big), setShadowColor (a dark copy one
 * step down and right makes text readable on any background), and setCentered(true)
 * to make x the middle of the text instead of its left edge.
 */
public class Label extends UIElement {

    private String text;
    private int color = Color.WHITE;
    private int shadowColor = Color.TRANSPARENT;
    private int scale = 1;
    private boolean centered = false;
    private Font font = null;    // null: the default font

    public Label(String text, int x, int y) {
        super(x, y, textWidth(text, null, 1), textHeight(text, null, 1));
        this.text = text;
    }

    @Override
    public void draw(Canvas canvas) {
        Font oldFont = canvas.getFont();
        if (font != null) {
            canvas.setFont(font);
        }
        int left = getLeft();
        if (!Color.isTransparent(shadowColor)) {
            canvas.drawText(text, left + scale, getY() + scale, shadowColor, scale);
        }
        canvas.drawText(text, left, getY(), color, scale);
        canvas.setFont(oldFont);
    }

    /** Where the text actually starts: x itself, or half the width to the left of x when centered. */
    private int getLeft() {
        if (centered) {
            return getX() - getWidth() / 2;
        }
        return getX();
    }

    @Override
    public boolean contains(int px, int py) {
        int left = getLeft();
        return px >= left && px < left + getWidth() && py >= getY() && py < getY() + getHeight();
    }

    /** Recalculates the width and height whenever the text, font or scale change. */
    private void measure() {
        setSize(textWidth(text, font, scale), textHeight(text, font, scale));
    }

    private static int textWidth(String text, Font font, int scale) {
        if (font == null) {
            font = Font.getDefault();
        }
        return font.getTextWidth(text) * scale;
    }

    private static int textHeight(String text, Font font, int scale) {
        if (font == null) {
            font = Font.getDefault();
        }
        return font.getTextHeight(text) * scale;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        measure();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    /** A copy of the text in this color is drawn one step down and right. Color.TRANSPARENT for none. */
    public void setShadowColor(int color) {
        shadowColor = color;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        if (scale < 1) {
            throw new IllegalArgumentException("Scale must be at least 1, got " + scale);
        }
        this.scale = scale;
        measure();
    }

    public boolean isCentered() {
        return centered;
    }

    /** When true, x is the middle of the text instead of its left edge. */
    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public void setFont(Font font) {
        this.font = font;
        measure();
    }
}
