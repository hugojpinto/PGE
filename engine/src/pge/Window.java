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
 * This is one of the few PGE files that use the JDK's window classes (AWT). It does
 * three things:
 *
 *   1. Opens a window (or goes fullscreen).
 *   2. Owns the pixel array that pge.gfx.Canvas draws into.
 *   3. Once per frame, shows that array in the window, scaled up to fill it (present()).
 *
 * The trick in step 2: we create a BufferedImage (a JDK image) and ask for the int[]
 * array inside it. The Canvas writes directly into that same array, so there is
 * nothing to copy: when present() shows the image, it already holds the new frame.
 */
class Window {

    /** Space kept free for the title bar and the dock or taskbar when choosing the starting size. */
    private static final int TITLE_BAR_ROOM = 64;

    private final int gameWidth;
    private final int gameHeight;

    private final Frame frame;
    // AWT has its own class called Canvas. To avoid confusing it with PGE's pge.gfx.Canvas,
    // we always write its full name and call our variable "surface".
    private final java.awt.Canvas surface;

    private final BufferedImage image;
    private final int[] pixels;
    private BufferStrategy strategy;

    private boolean fullscreen = false;
    private Rectangle windowedBounds;

    // Set on the event thread, read on the engine thread. "volatile" makes sure the
    // engine thread sees the change immediately.
    private volatile boolean closeRequested = false;

    Window(String title, int gameWidth, int gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        image = new BufferedImage(gameWidth, gameHeight, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int scale = startingScale();
        surface = new java.awt.Canvas();
        surface.setPreferredSize(new Dimension(gameWidth * scale, gameHeight * scale));
        surface.setBackground(java.awt.Color.BLACK);
        surface.setIgnoreRepaint(true);   // we draw it ourselves, every frame

        frame = new Frame(title);
        frame.setBackground(java.awt.Color.BLACK);
        frame.setIgnoreRepaint(true);
        frame.add(surface);
        frame.addWindowListener(new WindowEvents());
        frame.pack();
        frame.setLocationRelativeTo(null);   // center on screen
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
     * Shows the current frame: paints the window black, then draws the game image
     * as large as possible in the middle.
     *
     * We prefer whole-number scales (2x, 3x, 4x...) so every game pixel becomes a
     * perfect square of screen pixels. Only if the window is smaller than the game
     * do we shrink it by a fraction.
     */
    void present() {
        int windowWidth = surface.getWidth();
        int windowHeight = surface.getHeight();
        if (windowWidth <= 0 || windowHeight <= 0) {
            return;   // minimized, nothing to show
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

        // A BufferStrategy draws into a hidden buffer, then show() swaps it onto the
        // screen in one go, so the player never sees a half-drawn frame. The two loops
        // are the JDK's recommended way to handle the rare case where that hidden
        // buffer gets lost (for example when the screen mode changes).
        do {
            do {
                Graphics graphics = strategy.getDrawGraphics();
                graphics.setColor(java.awt.Color.BLACK);
                graphics.fillRect(0, 0, windowWidth, windowHeight);
                // Scale with "nearest neighbor": each game pixel becomes a crisp
                // square instead of a blurry blend of its neighbors.
                ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics.drawImage(image, left, top, drawWidth, drawHeight, null);
                graphics.dispose();
            } while (strategy.contentsRestored());
            strategy.show();
        } while (strategy.contentsLost());

        // On Linux, this makes sure the frame really reaches the screen now.
        Toolkit.getDefaultToolkit().sync();
    }

    /** Switches between a normal window and fullscreen. */
    void setFullscreen(boolean on) {
        if (on == fullscreen) {
            return;
        }
        GraphicsDevice screen = frame.getGraphicsConfiguration().getDevice();
        if (on) {
            windowedBounds = frame.getBounds();
            frame.dispose();                 // the title bar can only be removed while the window is closed
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
        createStrategy();                    // the old one was thrown away by dispose()
    }

    void close() {
        if (fullscreen) {
            frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
        }
        frame.dispose();
    }

    /** The biggest whole-number scale at which the game still fits on the screen. */
    private int startingScale() {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int scale = Math.min(usable.width / gameWidth, (usable.height - TITLE_BAR_ROOM) / gameHeight);
        if (scale < 1) {
            scale = 1;
        }
        return scale;
    }

    /** Two buffers: one on screen, one hidden that we draw into. */
    private void createStrategy() {
        surface.createBufferStrategy(2);
        strategy = surface.getBufferStrategy();
        surface.requestFocus();
    }

    /** Receives window events from the JDK. */
    private class WindowEvents extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent event) {
            closeRequested = true;
        }

        @Override
        public void windowActivated(WindowEvent event) {
            // Make sure key presses go to the game.
            surface.requestFocus();
        }
    }
}
