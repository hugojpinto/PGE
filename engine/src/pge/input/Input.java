package pge.input;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Asks "what is the player doing?" Call these from your update() method:
 *
 *   Input.isDown(Key.LEFT)        true while the key is held
 *   Input.wasPressed(Key.SPACE)   true only on the first update after the key went down
 *   Input.wasReleased(Key.SPACE)  true only on the first update after the key went up
 *
 * Use isDown for things that last (walking), wasPressed for things that happen once
 * per key press (jumping, shooting, choosing a menu item).
 *
 * HOW IT WORKS
 * The JDK reports key presses on its own thread (the "event thread"), at any moment,
 * while the game runs on the engine thread. To give the game a steady picture, events
 * are first written into "pending" arrays. Once per update, the engine calls
 * Input.update(), which copies them into "current" and keeps the old values in
 * "previous". Then:
 *
 *   isDown      = current
 *   wasPressed  = current and not previous
 *   wasReleased = previous and not current
 *
 * The methods that touch the pending arrays are "synchronized": Java then lets only
 * one thread run them at a time, so the two threads never trip over each other.
 */
public final class Input {

    /** Enough room for every ordinary key code. Rare codes above this are ignored. */
    private static final int KEY_COUNT = 1024;

    // Written by the event thread (always inside synchronized methods).
    private static final boolean[] pendingDown = new boolean[KEY_COUNT];
    private static final boolean[] pendingPressed = new boolean[KEY_COUNT];

    // Read by the game. Changed only by update(), on the engine thread.
    private static final boolean[] current = new boolean[KEY_COUNT];
    private static final boolean[] previous = new boolean[KEY_COUNT];

    private Input() {
    }

    // ------------------------------------------------------------------ for games

    /** True while the key is held down. */
    public static boolean isDown(int key) {
        if (!isValid(key)) {
            return false;
        }
        return current[key];
    }

    /** True only during the first update after the key went down. */
    public static boolean wasPressed(int key) {
        if (!isValid(key)) {
            return false;
        }
        return current[key] && !previous[key];
    }

    /** True only during the first update after the key went up. */
    public static boolean wasReleased(int key) {
        if (!isValid(key)) {
            return false;
        }
        return previous[key] && !current[key];
    }

    // ------------------------------------------------------------------ for the engine

    /** Called by the engine once, to start listening to a window. */
    public static void attachTo(Component component) {
        Listener listener = new Listener();
        component.addKeyListener(listener);
        component.addFocusListener(listener);
        // Without this, the JDK keeps TAB for itself (to move between buttons).
        component.setFocusTraversalKeysEnabled(false);
    }

    /** Called by the engine at the start of every update. */
    public static synchronized void update() {
        for (int key = 0; key < KEY_COUNT; key++) {
            previous[key] = current[key];
            // A very quick tap can go down AND up between two updates.
            // pendingPressed makes sure the game still sees it for one update.
            current[key] = pendingDown[key] || pendingPressed[key];
            pendingPressed[key] = false;
        }
    }

    // ------------------------------------------------------------------ event thread

    private static synchronized void keyWentDown(int key) {
        if (isValid(key)) {
            pendingDown[key] = true;
            pendingPressed[key] = true;
        }
    }

    private static synchronized void keyWentUp(int key) {
        if (isValid(key)) {
            pendingDown[key] = false;
        }
    }

    /** When the window loses focus we never hear about key releases, so let go of everything. */
    private static synchronized void releaseAll() {
        for (int key = 0; key < KEY_COUNT; key++) {
            pendingDown[key] = false;
            pendingPressed[key] = false;
        }
    }

    private static boolean isValid(int key) {
        return key >= 0 && key < KEY_COUNT;
    }

    /** Receives events from the JDK and passes them on to the methods above. */
    private static class Listener implements KeyListener, FocusListener {

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
    }
}
