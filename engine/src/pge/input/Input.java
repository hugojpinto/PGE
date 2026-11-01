package pge.input;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Asks "what is the player doing?" Call these from your update() method.
 *
 * KEYBOARD
 *   Input.isDown(Key.LEFT)        true while the key is held
 *   Input.wasPressed(Key.SPACE)   true only on the first update after the key went down
 *   Input.wasReleased(Key.SPACE)  true only on the first update after the key went up
 *
 * Use isDown for things that last (walking), wasPressed for things that happen once
 * per key press (jumping, shooting, choosing a menu item).
 *
 * POINTER (mouse, or a finger on a touch screen)
 *   Input.getPointerX(), getPointerY()   where it is, in game pixels (0 to 639 in a 640-wide game)
 *   Input.isPointerInside()              false when it is outside the game picture
 *   Input.isPointerDown()                left button held / finger on the screen
 *   Input.wasPointerPressed()            ... only on the first update
 *   Input.wasPointerReleased()           ... only on the first update after letting go
 *
 * The position is in screen coordinates. For a position in the world, ask the camera:
 * camera.toWorldX(Input.getPointerX()).
 *
 * MOUSE BUTTONS AND WHEEL
 *   Input.isMouseDown(Mouse.RIGHT), wasMousePressed(...), wasMouseReleased(...)
 *   Input.getWheel()    how many steps the wheel turned since the last update (+ down, - up)
 *
 * HOW IT WORKS
 * The JDK reports key presses and mouse moves on its own thread (the "event thread"),
 * at any moment, while the game runs on the engine thread. To give the game a steady
 * picture, events are first written into "pending" variables. Once per update, the
 * engine calls Input.update(), which copies them into "current" and keeps the old
 * values in "previous". Then:
 *
 *   isDown      = current
 *   wasPressed  = current and not previous
 *   wasReleased = previous and not current
 *
 * The methods that touch the pending variables are "synchronized": Java then lets only
 * one thread run them at a time, so the two threads never trip over each other.
 */
public final class Input {

    /** Enough room for every ordinary key code. Rare codes above this are ignored. */
    private static final int KEY_COUNT = 1024;
    /** Mouse buttons are numbered 1, 2, 3 (and some mice have a few more). */
    private static final int BUTTON_COUNT = 8;

    // ---- Written by the event thread (always inside synchronized methods).
    private static final boolean[] pendingKeyDown = new boolean[KEY_COUNT];
    private static final boolean[] pendingKeyPressed = new boolean[KEY_COUNT];
    private static final boolean[] pendingButtonDown = new boolean[BUTTON_COUNT];
    private static final boolean[] pendingButtonPressed = new boolean[BUTTON_COUNT];
    private static int pendingWindowX = 0;
    private static int pendingWindowY = 0;
    private static boolean pendingInWindow = false;
    private static int pendingWheel = 0;

    // ---- Where the game picture is inside the window. Set by the engine every frame.
    private static int areaLeft = 0;
    private static int areaTop = 0;
    private static int areaWidth = 1;
    private static int areaHeight = 1;
    private static int gameWidth = 1;
    private static int gameHeight = 1;

    // ---- Read by the game. Changed only by update(), on the engine thread.
    private static final boolean[] keyCurrent = new boolean[KEY_COUNT];
    private static final boolean[] keyPrevious = new boolean[KEY_COUNT];
    private static final boolean[] buttonCurrent = new boolean[BUTTON_COUNT];
    private static final boolean[] buttonPrevious = new boolean[BUTTON_COUNT];
    private static int pointerX = 0;
    private static int pointerY = 0;
    private static boolean pointerInside = false;
    private static int wheel = 0;

    private Input() {
    }

    // ================================================================== keyboard

    /** True while the key is held down. */
    public static boolean isDown(int key) {
        if (!isValidKey(key)) {
            return false;
        }
        return keyCurrent[key];
    }

    /** True only during the first update after the key went down. */
    public static boolean wasPressed(int key) {
        if (!isValidKey(key)) {
            return false;
        }
        return keyCurrent[key] && !keyPrevious[key];
    }

    /** True only during the first update after the key went up. */
    public static boolean wasReleased(int key) {
        if (!isValidKey(key)) {
            return false;
        }
        return keyPrevious[key] && !keyCurrent[key];
    }

    // ================================================================== pointer

    /** Pointer x in game pixels, measured from the left edge of the game picture. */
    public static int getPointerX() {
        return pointerX;
    }

    /** Pointer y in game pixels, measured from the top edge of the game picture. */
    public static int getPointerY() {
        return pointerY;
    }

    /** True if the pointer is over the game picture (not outside the window, not on the black bars). */
    public static boolean isPointerInside() {
        return pointerInside;
    }

    /** True while the left mouse button is held (or a finger touches the screen). */
    public static boolean isPointerDown() {
        return isMouseDown(Mouse.LEFT);
    }

    /** True only during the first update after the pointer went down. */
    public static boolean wasPointerPressed() {
        return wasMousePressed(Mouse.LEFT);
    }

    /** True only during the first update after the pointer was let go. */
    public static boolean wasPointerReleased() {
        return wasMouseReleased(Mouse.LEFT);
    }

    // ================================================================== mouse buttons and wheel

    public static boolean isMouseDown(int button) {
        if (!isValidButton(button)) {
            return false;
        }
        return buttonCurrent[button];
    }

    public static boolean wasMousePressed(int button) {
        if (!isValidButton(button)) {
            return false;
        }
        return buttonCurrent[button] && !buttonPrevious[button];
    }

    public static boolean wasMouseReleased(int button) {
        if (!isValidButton(button)) {
            return false;
        }
        return buttonPrevious[button] && !buttonCurrent[button];
    }

    /** How many steps the mouse wheel turned since the last update: positive is down, negative is up. */
    public static int getWheel() {
        return wheel;
    }

    // ================================================================== for the engine

    /** Called by the engine once, to start listening to a window. */
    public static void attachTo(Component component) {
        Listener listener = new Listener();
        component.addKeyListener(listener);
        component.addFocusListener(listener);
        component.addMouseListener(listener);
        component.addMouseMotionListener(listener);
        component.addMouseWheelListener(listener);
        // Without this, the JDK keeps TAB for itself (to move between buttons).
        component.setFocusTraversalKeysEnabled(false);
    }

    /**
     * Called by the engine every frame: where, inside the window, the game picture
     * was drawn (left, top, width, height), and how big the game is in game pixels.
     * update() uses this to turn window positions into game positions.
     */
    public static synchronized void setScreenArea(int left, int top, int width, int height,
                                                  int gameWidthInPixels, int gameHeightInPixels) {
        areaLeft = left;
        areaTop = top;
        areaWidth = Math.max(1, width);
        areaHeight = Math.max(1, height);
        gameWidth = gameWidthInPixels;
        gameHeight = gameHeightInPixels;
    }

    /** Called by the engine at the start of every update. */
    public static synchronized void update() {
        for (int key = 0; key < KEY_COUNT; key++) {
            keyPrevious[key] = keyCurrent[key];
            // A very quick tap can go down AND up between two updates.
            // pendingKeyPressed makes sure the game still sees it for one update.
            keyCurrent[key] = pendingKeyDown[key] || pendingKeyPressed[key];
            pendingKeyPressed[key] = false;
        }
        for (int button = 0; button < BUTTON_COUNT; button++) {
            buttonPrevious[button] = buttonCurrent[button];
            buttonCurrent[button] = pendingButtonDown[button] || pendingButtonPressed[button];
            pendingButtonPressed[button] = false;
        }

        // Window pixels to game pixels. Example: a 640-wide game drawn 1920 wide
        // starting at window x 0: window x 960 becomes (960 - 0) * 640 / 1920 = 320.
        pointerX = Math.floorDiv((pendingWindowX - areaLeft) * gameWidth, areaWidth);
        pointerY = Math.floorDiv((pendingWindowY - areaTop) * gameHeight, areaHeight);
        pointerInside = pendingInWindow
                && pointerX >= 0 && pointerX < gameWidth
                && pointerY >= 0 && pointerY < gameHeight;

        wheel = pendingWheel;
        pendingWheel = 0;
    }

    // ================================================================== event thread

    private static synchronized void keyWentDown(int key) {
        if (isValidKey(key)) {
            pendingKeyDown[key] = true;
            pendingKeyPressed[key] = true;
        }
    }

    private static synchronized void keyWentUp(int key) {
        if (isValidKey(key)) {
            pendingKeyDown[key] = false;
        }
    }

    private static synchronized void buttonWentDown(int button) {
        if (isValidButton(button)) {
            pendingButtonDown[button] = true;
            pendingButtonPressed[button] = true;
        }
    }

    private static synchronized void buttonWentUp(int button) {
        if (isValidButton(button)) {
            pendingButtonDown[button] = false;
        }
    }

    private static synchronized void pointerMoved(int windowX, int windowY) {
        pendingWindowX = windowX;
        pendingWindowY = windowY;
        pendingInWindow = true;
    }

    private static synchronized void pointerLeftWindow() {
        pendingInWindow = false;
    }

    private static synchronized void wheelTurned(int steps) {
        pendingWheel = pendingWheel + steps;
    }

    /** When the window loses focus we never hear about releases, so let go of everything. */
    private static synchronized void releaseAll() {
        for (int key = 0; key < KEY_COUNT; key++) {
            pendingKeyDown[key] = false;
            pendingKeyPressed[key] = false;
        }
        for (int button = 0; button < BUTTON_COUNT; button++) {
            pendingButtonDown[button] = false;
            pendingButtonPressed[button] = false;
        }
    }

    private static boolean isValidKey(int key) {
        return key >= 0 && key < KEY_COUNT;
    }

    private static boolean isValidButton(int button) {
        return button >= 0 && button < BUTTON_COUNT;
    }

    /** Receives events from the JDK and passes them on to the methods above. */
    private static class Listener implements KeyListener, FocusListener,
            MouseListener, MouseMotionListener, MouseWheelListener {

        @Override
        public void keyPressed(KeyEvent event) {
            keyWentDown(event.getKeyCode());
        }

        @Override
        public void keyReleased(KeyEvent event) {
            keyWentUp(event.getKeyCode());
        }

        @Override
        public void keyTyped(KeyEvent event) {
            // Not needed: we care about keys, not about the characters they type.
        }

        @Override
        public void focusGained(FocusEvent event) {
            // Nothing to do.
        }

        @Override
        public void focusLost(FocusEvent event) {
            releaseAll();
        }

        @Override
        public void mousePressed(MouseEvent event) {
            pointerMoved(event.getX(), event.getY());
            buttonWentDown(event.getButton());
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            pointerMoved(event.getX(), event.getY());
            buttonWentUp(event.getButton());
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            pointerMoved(event.getX(), event.getY());
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            pointerMoved(event.getX(), event.getY());
        }

        @Override
        public void mouseEntered(MouseEvent event) {
            pointerMoved(event.getX(), event.getY());
        }

        @Override
        public void mouseExited(MouseEvent event) {
            pointerLeftWindow();
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            // Not needed: pressed and released already tell us everything.
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            wheelTurned(event.getWheelRotation());
        }
    }
}
