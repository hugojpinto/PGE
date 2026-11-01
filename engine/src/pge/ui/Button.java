package pge.ui;

import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;

/**
 * A box with text that can be clicked (or tapped).
 *
 *   Button play = new Button("Play", 270, 200, 100, 24);
 *   addUI(play);
 *
 *   // in update():
 *   if (play.wasClicked()) {
 *       Engine.setScene(new Level1());
 *   }
 *
 * You ask the button whether it was clicked, the same way you ask Input whether a
 * key was pressed. Desktop programs usually do it the other way round: you give the
 * button a "listener", a piece of code it runs when it is clicked. That needs extra
 * classes or lambdas, and the code that runs ends up far from the rest of the game's
 * logic. Asking in update() keeps everything in one place, in the order it happens.
 *
 * A click counts when the pointer goes down on the button and is let go on the
 * button too. Moving off the button before letting go cancels the click. This is how
 * buttons behave in almost every program, so players expect it: it lets you change
 * your mind halfway through a click.
 *
 * The button looks different while the pointer is over it (hover) and while it is
 * held down (pressed). Change the colours with the setters below.
 */
public class Button extends UIElement {

    private String text;
    private int textColor = Color.WHITE;
    private int borderColor = Color.WHITE;
    private int normalColor = Color.DARK_GRAY;
    private int hoverColor = Color.GRAY;
    private int pressedColor = Color.BLACK;

    private boolean hovered = false;
    private boolean pressed = false;     // went down on this button and not let go yet
    private boolean clicked = false;     // true for one update after a click

    public Button(String text, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.text = text;
    }

    @Override
    public void update() {
        clicked = false;
        hovered = Input.isPointerInside() && contains(Input.getPointerX(), Input.getPointerY());

        if (hovered && Input.wasPointerPressed()) {
            pressed = true;
        }
        if (Input.wasPointerReleased()) {
            if (pressed && hovered) {
                clicked = true;
            }
            pressed = false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        int fill = normalColor;
        if (pressed && hovered) {
            fill = pressedColor;
        } else if (hovered) {
            fill = hoverColor;
        }
        canvas.fillRect(getX(), getY(), getWidth(), getHeight(), fill);
        canvas.drawRect(getX(), getY(), getWidth(), getHeight(), borderColor);

        // Centre the text inside the button. While pressed, the text moves down one
        // pixel, which makes the button look as if it is being pushed in.
        int textX = getX() + (getWidth() - canvas.getTextWidth(text)) / 2;
        int textY = getY() + (getHeight() - canvas.getTextHeight(text)) / 2;
        if (pressed && hovered) {
            textY = textY + 1;
        }
        canvas.drawText(text, textX, textY, textColor);
    }

    /** True for one update, right after the button was clicked. */
    public boolean wasClicked() {
        return clicked;
    }

    /** True while the pointer is over the button. */
    public boolean isHovered() {
        return hovered;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public void setBorderColor(int color) {
        borderColor = color;
    }

    /** The fill colours: normally, with the pointer over it, and while held down. */
    public void setColors(int normal, int hover, int pressed) {
        normalColor = normal;
        hoverColor = hover;
        pressedColor = pressed;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // A hidden button forgets any half-finished click, so it cannot fire a click
        // later, the moment it is shown again.
        pressed = false;
        clicked = false;
    }
}
