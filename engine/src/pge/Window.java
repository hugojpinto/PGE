package pge;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import pge.input.Input;

/**
 * The window the game appears in.
 *
 * This is one of the few PGE files that use Java's window classes. They come from AWT,
 * the "Abstract Window Toolkit", which has been part of Java since its first version
 * in 1996. (Swing, Java's better-known set of buttons and menus, is built on top of
 * AWT.) This class does three things:
 *
 *   1. It opens a window, or switches to fullscreen.
 *   2. It owns the array of pixels that pge.gfx.Canvas draws into.
 *   3. Once per frame, present() shows that array in the window, made bigger to fill it.
 *
 * How step 2 works: we create a BufferedImage, which is Java's class for a picture
 * kept in memory, and ask it for the int[] array where it stores its colours. The
 * Canvas writes straight into that same array. So when present() shows the image,
 * the new frame is already in it, and nothing needs to be copied.
 *
 * The class has no "public" in front of it, so only classes in the package pge can
 * use it. Games never need to; they go through Engine instead.
 */
class Window {

    /**
     * Space left free for the title bar and the dock or taskbar when choosing how big
     * the window starts.
     */
    private static final int TITLE_BAR_ROOM = 64;

    private final int gameWidth;
    private final int gameHeight;

    // A Frame is AWT's name for a normal window with a title bar and a close button.
    private final Frame frame;
    // AWT has its own class called Canvas: an empty area inside a window that a program
    // can draw on. To avoid mixing it up with PGE's pge.gfx.Canvas, we always write its
    // full name, java.awt.Canvas, and call our variable "surface".
    private final java.awt.Canvas surface;

    private final BufferedImage image;
    private final int[] pixels;
    private BufferStrategy strategy;

    private boolean fullscreen = false;
    private Rectangle windowedBounds;    // where the window was before going fullscreen

    // This flag is set by Java's event thread (when the close button is clicked) and
    // read by the thread that runs the game loop. Normally a thread is allowed to keep
    // its own copy of a variable for speed, and might not notice another thread's
    // change for a while. "volatile" tells Java not to do that for this variable, so
    // the game loop sees the change straight away.
    private volatile boolean closeRequested = false;

    Window(String title, int gameWidth, int gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        // TYPE_INT_RGB stores each pixel as one int, 0xRRGGBB: the same format as our colours.
        image = new BufferedImage(gameWidth, gameHeight, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int scale = startingScale();
        surface = new java.awt.Canvas();
        surface.setPreferredSize(new Dimension(gameWidth * scale, gameHeight * scale));
        surface.setBackground(java.awt.Color.BLACK);
        // Normally AWT redraws a window whenever it thinks it needs it (for example after
        // another window was on top of it). We draw a new frame 60 times a second anyway,
        // so we tell it not to bother.
        surface.setIgnoreRepaint(true);

        frame = new Frame(title);
        frame.setBackground(java.awt.Color.BLACK);
        frame.setIgnoreRepaint(true);
        frame.add(surface);
        frame.addWindowListener(new WindowEvents());
        frame.pack();                        // make the window just big enough for the surface
        frame.setLocationRelativeTo(null);   // null means: centre it on the screen
        frame.setVisible(true);

        Input.attachTo(surface);
        createStrategy();
    }

    /** The pixel array inside the image. pge.gfx.Canvas draws into it. */
    int[] getPixels() {
        return pixels;
    }

    void setTitle(String title) {
        frame.setTitle(title);
    }

    boolean isCloseRequested() {
        return closeRequested;
    }

    boolean isFullscreen() {
        return fullscreen;
    }

    /**
     * Shows the current frame: paints the window black, then draws the game picture as
     * big as possible in the middle.
     *
     * We make the picture bigger by a whole number (2, 3, 4...), so every game pixel
     * becomes a perfect square of screen pixels. A scale of 2.5 would make some game
     * pixels 2 screen pixels wide and others 3, and pixel art would look uneven. Space
     * that is left over becomes black bars around the picture. Films do the same when
     * a wide film is shown on a narrower screen, which is why this is called
     * "letterboxing". Only when the window is smaller than the game picture do we
     * shrink it by a fraction.
     */
    void present() {
        int windowWidth = surface.getWidth();
        int windowHeight = surface.getHeight();
        if (windowWidth <= 0 || windowHeight <= 0) {
            return;   // the window is minimized: there is nothing to show
        }

        int scale = Math.min(windowWidth / gameWidth, windowHeight / gameHeight);
        int drawWidth;
        int drawHeight;
        if (scale >= 1) {
            drawWidth = gameWidth * scale;
            drawHeight = gameHeight * scale;
        } else {
            double fit = Math.min((double) windowWidth / gameWidth, (double) windowHeight / gameHeight);
            drawWidth = (int) (gameWidth * fit);
            drawHeight = (int) (gameHeight * fit);
        }
        int left = (windowWidth - drawWidth) / 2;
        int top = (windowHeight - drawHeight) / 2;

        // Input needs to know where the picture is, to turn mouse positions into game positions.
        Input.setScreenArea(left, top, drawWidth, drawHeight, gameWidth, gameHeight);

        // "Double buffering": we draw into a hidden copy of the window (the back
        // buffer), and show() then swaps it with the visible one in one go. Drawing
        // straight onto the visible window would let the player see a half-finished
        // frame, which shows up as flickering or as a "tear" line across the picture.
        // The two do-while loops are the pattern Java's documentation recommends for
        // BufferStrategy: they draw the frame again in the rare case that the hidden
        // buffer's memory was lost, for example when the screen mode changed.
        do {
            do {
                Graphics graphics = strategy.getDrawGraphics();
                graphics.setColor(java.awt.Color.BLACK);
                graphics.fillRect(0, 0, windowWidth, windowHeight);
                // When a picture is made bigger, the computer must decide what colour
                // the new in-between pixels get. "Nearest neighbour" simply copies the
                // closest original pixel, so each game pixel becomes a sharp square.
                // The other choices blend neighbouring colours, which looks smooth for
                // photos but makes pixel art blurry.
                ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics.drawImage(image, left, top, drawWidth, drawHeight, null);
                graphics.dispose();    // free the drawing tools we were given
            } while (strategy.contentsRestored());
            strategy.show();
        } while (strategy.contentsLost());

        // On Linux, AWT can hold drawing commands back and send them to the screen
        // later in a batch. sync() sends them now, so the frame appears on time.
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Switches between a normal window and fullscreen, using the "full-screen
     * exclusive mode" that Java has offered since version 1.4.
     */
    void setFullscreen(boolean on) {
        if (on == fullscreen) {
            return;
        }
        GraphicsDevice screen = frame.getGraphicsConfiguration().getDevice();
        if (on) {
            windowedBounds = frame.getBounds();
            // A fullscreen window should have no title bar. AWT only lets us remove the
            // title bar (the "decorations") while the window is closed, so we close it
            // with dispose(), change it, and let setFullScreenWindow open it again.
            frame.dispose();
            frame.setUndecorated(true);
            screen.setFullScreenWindow(frame);
        } else {
            screen.setFullScreenWindow(null);
            frame.dispose();
            frame.setUndecorated(false);
            frame.setBounds(windowedBounds);
            frame.setVisible(true);
        }
        fullscreen = on;
        createStrategy();    // dispose() also threw away the old buffers, so we need new ones
    }

    void close() {
        if (fullscreen) {
            frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
        }
        frame.dispose();
    }

    /**
     * The biggest whole-number scale at which the game still fits on the screen.
     * getMaximumWindowBounds() gives the part of the screen that windows may use,
     * which leaves out the macOS menu bar and the Windows taskbar.
     */
    private int startingScale() {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int scale = Math.min(usable.width / gameWidth, (usable.height - TITLE_BAR_ROOM) / gameHeight);
        if (scale < 1) {
            scale = 1;
        }
        return scale;
    }

    /**
     * Creates the two buffers for double buffering: the one on screen and the hidden
     * one we draw into. Then asks for keyboard focus, so key presses come to us.
     */
    private void createStrategy() {
        surface.createBufferStrategy(2);
        strategy = surface.getBufferStrategy();
        surface.requestFocus();
    }

    /**
     * Receives window events from Java. WindowAdapter is a class that has an empty
     * method for every kind of window event, so we only write the ones we need.
     *
     * This class is written inside Window (an "inner class"), so it can use Window's
     * fields, such as closeRequested and surface.
     */
    private class WindowEvents extends WindowAdapter {

        /** The close button was clicked. The game loop sees the flag and ends the game. */
        @Override
        public void windowClosing(WindowEvent event) {
            closeRequested = true;
        }

        /** The window became the active window again: make sure key presses come to the game. */
        @Override
        public void windowActivated(WindowEvent event) {
            surface.requestFocus();
        }
    }
}
