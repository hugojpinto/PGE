package pge;

import pge.gfx.Canvas;
import pge.input.Input;
import pge.input.Key;

/**
 * Starts and runs a game.
 *
 *   public static void main(String[] args) {
 *       Engine.run(new MyGame(), 640, 360);
 *   }
 *
 * The width and height are the size of the game's picture in pixels, not the size of
 * the window. The window is a whole-number multiple of it (640x360 becomes 1920x1080
 * at 3x), so the pixels stay crisp at any window size.
 *
 * Built-in keys, available in every game:
 *   F11, or Alt+Enter (Option+Enter on a Mac)   toggle fullscreen
 *   F3                                           show speed statistics in the title bar
 *
 * THE GAME LOOP
 * The heart of every game engine. Ours runs update() exactly 60 times per second,
 * whatever the speed of the computer, and draws after each round of updates:
 *
 *   repeat until the window closes:
 *       wait until the next update is due
 *       Input.update(), then game.update()   (more than once if we fell behind)
 *       game.draw(canvas), then show the canvas in the window
 *
 * Because the update rate never changes, "x = x + 2" always means "2 pixels per
 * update" on every computer. No time calculations needed.
 */
public final class Engine {

    public static final int UPDATES_PER_SECOND = 60;

    private static final long NANOS_PER_SECOND = 1000000000L;
    private static final long NANOS_PER_UPDATE = NANOS_PER_SECOND / UPDATES_PER_SECOND;
    // If the computer falls far behind (say, it froze for a second), don't try to catch
    // up with dozens of updates at once. A few, then carry on from "now".
    private static final int MAX_UPDATES_PER_FRAME = 5;

    private static Window window;
    private static Canvas canvas;
    private static String title;
    private static boolean running = false;
    private static long updateCount = 0;

    // Statistics (F3), measured over the last second.
    private static boolean showStats = false;
    private static int framesPerSecond = 0;
    private static double updateMillis = 0;
    private static double drawMillis = 0;

    private Engine() {
    }

    // ------------------------------------------------------------------ starting

    /** Opens a window titled "PGE" and runs the game until the window is closed. */
    public static void run(Game game, int width, int height) {
        run(game, width, height, "PGE");
    }

    /** Opens a window with the given title and runs the game until the window is closed. */
    public static void run(Game game, int width, int height, String windowTitle) {
        if (window != null) {
            throw new IllegalStateException("Engine.run was already called. A program can only run one game.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("The game size must be positive, got " + width + "x" + height);
        }

        title = windowTitle;
        window = new Window(title, width, height);
        canvas = new Canvas(width, height, window.getPixels());

        try {
            game.init();
            loop(game);
        } catch (Throwable problem) {
            // Something in the game crashed. Close the window instead of leaving it
            // frozen on screen, then show what went wrong.
            window.close();
            problem.printStackTrace();
            System.exit(1);
        }
        window.close();
        System.exit(0);
    }

    // ------------------------------------------------------------------ for games

    /** Ends the game: the window closes after the current update. */
    public static void stop() {
        running = false;
    }

    /** Width of the game picture, in game pixels. */
    public static int getWidth() {
        return canvas.getWidth();
    }

    /** Height of the game picture, in game pixels. */
    public static int getHeight() {
        return canvas.getHeight();
    }

    /** How many updates have run since the game started. At 60 per second, 600 means 10 seconds. */
    public static long getUpdateCount() {
        return updateCount;
    }

    /** How many frames were shown during the last second. */
    public static int getFramesPerSecond() {
        return framesPerSecond;
    }

    public static void setTitle(String windowTitle) {
        title = windowTitle;
        window.setTitle(windowTitle);
    }

    public static boolean isFullscreen() {
        return window.isFullscreen();
    }

    public static void setFullscreen(boolean on) {
        window.setFullscreen(on);
    }

    // ------------------------------------------------------------------ the loop

    private static void loop(Game game) {
        running = true;
        long nextUpdate = System.nanoTime();

        // For the statistics
        long statsStart = System.nanoTime();
        int frames = 0;
        long updateNanos = 0;
        int updates = 0;
        long drawNanos = 0;

        while (running && !window.isCloseRequested()) {
            long now = System.nanoTime();
            if (now < nextUpdate) {
                waitFor(nextUpdate - now);
                continue;
            }

            // 1. Update: once, or a few times if we are behind schedule.
            int updatesThisFrame = 0;
            while (now >= nextUpdate && updatesThisFrame < MAX_UPDATES_PER_FRAME && running) {
                long updateStart = System.nanoTime();
                Input.update();
                handleEngineKeys();
                game.update();
                updateCount = updateCount + 1;
                updateNanos = updateNanos + (System.nanoTime() - updateStart);
                updates = updates + 1;

                nextUpdate = nextUpdate + NANOS_PER_UPDATE;
                updatesThisFrame = updatesThisFrame + 1;
            }
            if (now >= nextUpdate) {
                nextUpdate = now + NANOS_PER_UPDATE;   // too far behind: skip ahead
            }

            // 2. Draw. Every frame starts without a camera offset.
            long drawStart = System.nanoTime();
            canvas.resetCamera();
            game.draw(canvas);
            window.present();
            drawNanos = drawNanos + (System.nanoTime() - drawStart);
            frames = frames + 1;

            // 3. Once per second, update the statistics.
            if (System.nanoTime() - statsStart >= NANOS_PER_SECOND) {
                framesPerSecond = frames;
                updateMillis = updateNanos / 1000000.0 / Math.max(updates, 1);
                drawMillis = drawNanos / 1000000.0 / Math.max(frames, 1);
                if (showStats) {
                    window.setTitle(statsText());
                }
                statsStart = System.nanoTime();
                frames = 0;
                updates = 0;
                updateNanos = 0;
                drawNanos = 0;
            }
        }
    }

    /** Keys that work in every game: fullscreen and statistics. */
    private static void handleEngineKeys() {
        boolean altEnter = Input.isDown(Key.ALT) && Input.wasPressed(Key.ENTER);
        if (Input.wasPressed(Key.F11) || altEnter) {
            window.setFullscreen(!window.isFullscreen());
        }
        if (Input.wasPressed(Key.F3)) {
            showStats = !showStats;
            if (showStats) {
                window.setTitle(statsText());
            } else {
                window.setTitle(title);
            }
        }
    }

    private static String statsText() {
        return String.format("%s  |  %d fps  |  update %.2f ms  |  draw %.2f ms",
                title, framesPerSecond, updateMillis, drawMillis);
    }

    /**
     * Waits until the next update is due. Thread.sleep is not very precise, so we sleep
     * for most of the time and then briefly yield until the moment arrives.
     */
    private static void waitFor(long nanos) {
        long millis = nanos / 1000000;
        if (millis >= 2) {
            try {
                Thread.sleep(millis - 1);
            } catch (InterruptedException e) {
                running = false;
            }
        } else {
            Thread.yield();
        }
    }
}
