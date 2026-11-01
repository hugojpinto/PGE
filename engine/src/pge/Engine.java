package pge;

import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;
import pge.scene.Scene;
import pge.scene.SceneRunner;

/**
 * Starts a game and keeps it running.
 *
 *   public static void main(String[] args) {
 *       Engine.run(new MyGame(), 640, 360);
 *   }
 *
 * MyGame can be a Game, where you write update() and draw() yourself, or a Scene,
 * where you add objects and the scene updates and draws them. A game made of scenes
 * changes screens with Engine.setScene(new OtherScene()).
 *
 * The width and height are the size of the game's picture in pixels, not the size of
 * the window. The window shows the picture made bigger by a whole number (640x360
 * becomes 1920x1080 at 3 times), so every game pixel stays a sharp square.
 *
 * Keys that work in every game:
 *   F11, or Alt+Enter (Option+Enter on a Mac)   switch fullscreen on or off
 *   F3                                           show how long each frame takes
 *
 * THE GAME LOOP
 * Every game engine has a loop at its centre that runs until the game ends. Each time
 * round, it reads the input, moves the game forward and draws a new picture. Ours
 * calls update() exactly 60 times per second, however fast the computer is:
 *
 *   repeat until the window closes:
 *       wait until the next update is due
 *       Input.update(), then game.update()   (several times, if we have fallen behind)
 *       game.draw(canvas), then show the canvas in the window
 *
 * This is called a "fixed time step". Many early games simply ran as fast as the
 * computer allowed, which is why some old PC games became unplayably fast on newer
 * machines. Other engines measure the time since the last update (the "delta time")
 * and multiply every speed by it. A fixed step is simpler: "x = x + 2" always means
 * 2 pixels per update on every computer, and a jump is always the same height.
 * Pico-8 works this way too. The technique of running fixed steps while drawing
 * whenever possible is described in Glenn Fiedler's well-known article
 * "Fix Your Timestep!" (2004).
 */
public final class Engine {

    public static final int UPDATES_PER_SECOND = 60;

    // System.nanoTime() counts in nanoseconds: a billion of them per second.
    private static final long NANOS_PER_SECOND = 1000000000L;
    private static final long NANOS_PER_UPDATE = NANOS_PER_SECOND / UPDATES_PER_SECOND;
    // If the computer falls far behind (for example, it froze for a second), we do not
    // try to catch up with dozens of updates at once: the game would appear to jump
    // forward. We run a few, then carry on from "now".
    private static final int MAX_UPDATES_PER_FRAME = 5;

    private static Window window;
    private static SceneRunner sceneRunner = null;
    private static Canvas canvas;
    private static String title;
    private static boolean running = false;
    private static long updateCount = 0;

    // The F3 statistics, measured over the last second.
    private static boolean showStats = false;
    private static int framesPerSecond = 0;
    private static double updateMillis = 0;
    private static double drawMillis = 0;

    /** Nobody needs an Engine object: everything here is static, because there is only one game. */
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
        // The canvas draws straight into the window's picture: they share one array.
        canvas = new Canvas(width, height, window.getPixels());

        try {
            game.init();
            loop(game);
        } catch (Throwable problem) {
            // Throwable catches every kind of error, even serious ones like running out
            // of memory or a StackOverflowError. Normally you should not catch those,
            // but here we only want to close the window properly before the program
            // ends; otherwise it would stay on the screen, frozen. Then we print the
            // error so the programmer can see what went wrong.
            window.close();
            problem.printStackTrace();
            System.exit(1);    // an exit code other than 0 tells the terminal something went wrong
        }
        window.close();
        System.exit(0);
    }

    /** Runs a game made of scenes, starting with this one, in a window titled "PGE". */
    public static void run(Scene firstScene, int width, int height) {
        run(firstScene, width, height, "PGE");
    }

    /**
     * Runs a game made of scenes, starting with this one. The scene is wrapped in a
     * SceneRunner, which is a Game, so from here on it runs like any other game.
     */
    public static void run(Scene firstScene, int width, int height, String windowTitle) {
        sceneRunner = new SceneRunner(firstScene);
        run(sceneRunner, width, height, windowTitle);
    }

    /** Switches to another scene. The switch happens at the start of the next update. */
    public static void setScene(Scene scene) {
        if (sceneRunner == null) {
            throw new IllegalStateException("Engine.setScene only works in games started with a Scene: "
                    + "Engine.run(new MyScene(), width, height)");
        }
        sceneRunner.changeScene(scene);
    }

    /** The scene that is running now, or null if the game was not started with a Scene. */
    public static Scene getScene() {
        if (sceneRunner == null) {
            return null;
        }
        return sceneRunner.getCurrentScene();
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

    /**
     * The game loop itself. nextUpdate is the moment, in nanoseconds, when the next
     * update should run. Each update moves it forward by exactly 1/60 of a second, so
     * small delays even out: if one update starts a little late, the next one is
     * simply due a little sooner.
     *
     * We use System.nanoTime() rather than System.currentTimeMillis(). The second one
     * is the computer's clock, which can jump when it is corrected over the internet
     * or changed by hand. nanoTime only ever moves forward, which is what measuring
     * short times needs.
     */
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
                nextUpdate = now + NANOS_PER_UPDATE;   // too far behind: give up catching up
            }

            // 2. Draw. Every frame starts without a camera offset.
            long drawStart = System.nanoTime();
            canvas.resetCamera();
            game.draw(canvas);
            if (showStats) {
                drawStats();
            }
            window.present();
            drawNanos = drawNanos + (System.nanoTime() - drawStart);
            frames = frames + 1;

            // 3. Once per second, work out the statistics for the F3 overlay.
            if (System.nanoTime() - statsStart >= NANOS_PER_SECOND) {
                framesPerSecond = frames;
                updateMillis = updateNanos / 1000000.0 / Math.max(updates, 1);
                drawMillis = drawNanos / 1000000.0 / Math.max(frames, 1);
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
        }
    }

    /**
     * The F3 overlay, in the top-right corner. At 60 frames per second, each frame has
     * 1000 / 60 = 16.7 milliseconds in total. "update" and "draw" show how much of that
     * the game is using. If the two together get close to 16.7, the game will start to
     * slow down.
     */
    private static void drawStats() {
        String text = String.format("%d FPS\nupdate %.2f ms\ndraw   %.2f ms",
                framesPerSecond, updateMillis, drawMillis);
        int boxWidth = canvas.getTextWidth(text) + 8;
        int boxHeight = canvas.getTextHeight(text) + 8;
        int left = canvas.getWidth() - boxWidth - 4;
        int top = 4;
        canvas.resetCamera();
        canvas.fillRect(left, top, boxWidth, boxHeight, Color.BLACK);
        canvas.drawRect(left, top, boxWidth, boxHeight, Color.GRAY);
        canvas.drawText(text, left + 4, top + 4, Color.GREEN);
    }

    /**
     * Waits until the next update is due.
     *
     * Thread.sleep(n) asks the operating system to wake us up after n milliseconds. The
     * operating system shares the processor between many programs in small time
     * slices, so it often wakes us up a little late, sometimes by more than a
     * millisecond. To be on time, we sleep for all but the last millisecond. When less
     * than that is left, we call Thread.yield(), which only gives other programs a
     * quick turn, and then the loop checks the clock again.
     */
    private static void waitFor(long nanos) {
        long millis = nanos / 1000000;
        if (millis >= 2) {
            try {
                Thread.sleep(millis - 1);
            } catch (InterruptedException e) {
                // Another thread asked this one to stop waiting. Nothing in PGE does that,
                // so if it happens, the safest thing is to end the game.
                running = false;
            }
        } else {
            Thread.yield();
        }
    }
}
