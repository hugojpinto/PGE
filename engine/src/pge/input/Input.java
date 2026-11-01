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
 * EVENTS AND POLLING
 * There are two ways for a program to find out about input. With "events", the
 * system calls a method of yours every time something happens: a key goes down, the
 * mouse moves. Desktop programs work this way, and so does Java's window system: it
 * sends KeyEvents and MouseEvents to "listener" objects. With "polling", the program
 * asks when it is ready: "is the left key down right now?" Games usually poll,
 * because they already do everything once per update anyway.
 *
 * This class turns Java's events into answers you can poll. The listener at the
 * bottom of this file receives the events and writes them down; the methods at the
 * top answer the game's questions.
 *
 * HOW IT WORKS
 * Java delivers key presses and mouse movements on its own thread, the "event
 * thread", at any moment, while the game runs on another thread. (A thread is a
 * sequence of instructions that runs at the same time as others; see Engine and
 * Window.) If the game read the event thread's variables directly, a key could change
 * halfway through an update, and two parts of the same update could disagree about
 * whether it was pressed.
 *
 * So events are first written into "pending" variables. Once per update, the engine
 * calls Input.update(), which copies them into "current" and moves the old values into
 * "previous". The game only reads "current" and "previous", which stay the same for
 * the whole update. Then:
 *
 *   isDown      = current
 *   wasPressed  = current and not previous     (it has just gone down)
 *   wasReleased = previous and not current     (it has just gone up)
 *
 * Electronics engineers call this "edge detection": noticing the moment a signal
 * changes from off to on (a "rising edge") or from on to off (a "falling edge"),
 * rather than whether it is on.
 *
 * The methods that use the pending variables are marked "synchronized". Java then
 * lets only one thread at a time run any of them, so the event thread can never be
 * halfway through writing while the engine thread is copying.
 */
public final class Input {

    /**
     * Enough room for every ordinary key code. Java numbers keys with "virtual key
     * codes" (KeyEvent.VK_A, KeyEvent.VK_SPACE...), and all the usual ones are below
     * 1024. The rare codes above that, for unusual keys, are ignored.
     */
    private static final int KEY_COUNT = 1024;
    /** Mouse buttons are numbered 1 (left), 2 (middle) and 3 (right). Some mice have a few more. */
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

    // ---- Read by the game. Changed only by update(), on the thread that runs the game.
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

    /**
     * Called by the engine once, to start listening to a window. One Listener object
     * receives all five kinds of events, because it implements all five listener
     * interfaces (see the class at the bottom of this file).
     */
    public static void attachTo(Component component) {
        Listener listener = new Listener();
        component.addKeyListener(listener);
        component.addFocusListener(listener);
        component.addMouseListener(listener);
        component.addMouseMotionListener(listener);
        component.addMouseWheelListener(listener);
        // Normally Java uses TAB to move the "focus" from one button or text box to the
        // next, and never passes the key on. A game wants to use TAB itself.
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

    /**
     * Called by the engine at the start of every update. Copies the pending state into
     * the current state, so the game sees one steady picture for the whole update.
     */
    public static synchronized void update() {
        for (int key = 0; key < KEY_COUNT; key++) {
            keyPrevious[key] = keyCurrent[key];
            // A very quick tap can go down AND up between two updates (they are 16.7
            // milliseconds apart). Looking only at pendingKeyDown would miss it.
            // pendingKeyPressed remembers that the key went down at some point since
            // the last update, so the game still sees it as down for one update.
            keyCurrent[key] = pendingKeyDown[key] || pendingKeyPressed[key];
            pendingKeyPressed[key] = false;
        }
        for (int button = 0; button < BUTTON_COUNT; button++) {
            buttonPrevious[button] = buttonCurrent[button];
            buttonCurrent[button] = pendingButtonDown[button] || pendingButtonPressed[button];
            pendingButtonPressed[button] = false;
        }

        // Turn window pixels into game pixels. First subtract where the picture starts
        // inside the window, then scale down by how much the picture was enlarged.
        // Example: a 640-wide game drawn 1920 wide, starting at window x 0. The mouse at
        // window x 960 becomes (960 - 0) * 640 / 1920 = 320, the middle of the game.
        // We multiply before dividing, so that integer division loses as little as
        // possible. Math.floorDiv rounds down even for negative numbers, so a mouse just
        // left of the picture gives -1 (outside) rather than 0 (inside).
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

    /**
     * Called when our window stops being the active window (the user clicked another
     * program). From then on, Java sends key events to the other program, so we would
     * never hear that a key was let go, and the game would think it is still held.
     * To be safe, we let go of everything.
     */
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

    /**
     * Receives events from Java and passes them on to the methods above. Java calls
     * these methods on the event thread whenever something happens.
     *
     * A listener must have every method of each interface it implements, even the ones
     * we do not need; those are left empty. The "static" in front of "class" means this
     * nested class does not belong to any particular Input object (there are none:
     * everything in Input is static).
     */
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
