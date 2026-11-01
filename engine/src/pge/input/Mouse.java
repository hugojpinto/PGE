package pge.input;

import java.awt.event.MouseEvent;

/**
 * Names for mouse buttons, to use with Input.isMouseDown(...), Input.wasMousePressed(...)
 * and Input.wasMouseReleased(...).
 *
 * The "pointer" methods of Input (isPointerDown and the others) use the LEFT button.
 * On touch screens, the operating system reports a finger as if it were the left
 * mouse button, so games that use the pointer methods also work with touch.
 *
 * Java numbers the buttons 1, 2 and 3 (MouseEvent.BUTTON1 and so on), in the order
 * left, middle, right. The middle button is usually the scroll wheel, which can also
 * be pressed down.
 */
public final class Mouse {

    public static final int LEFT = MouseEvent.BUTTON1;
    public static final int MIDDLE = MouseEvent.BUTTON2;
    public static final int RIGHT = MouseEvent.BUTTON3;

    private Mouse() {
    }
}
