package pge.input;

import java.awt.event.MouseEvent;

/**
 * Names for mouse buttons, to use with Input.isMouseDown(...), Input.wasMousePressed(...)
 * and Input.wasMouseReleased(...).
 *
 * The "pointer" methods of Input (isPointerDown and friends) use the LEFT button.
 * On touch screens, a finger also counts as the left button.
 */
public final class Mouse {

    public static final int LEFT = MouseEvent.BUTTON1;
    public static final int MIDDLE = MouseEvent.BUTTON2;
    public static final int RIGHT = MouseEvent.BUTTON3;

    private Mouse() {
    }
}
