import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import pge.Engine;
import pge.audio.Sound;
import pge.gfx.Bitmap;
import pge.gfx.Canvas;
import pge.gfx.Color;

/**
 * Makes the pictures in the README (docs/images/) by playing the example games by
 * itself: it starts each game, presses keys for it, and saves screenshots along the
 * way. Then it draws the hero banner, using PGE's own Canvas.
 *
 *   java Build.java                 compile everything first
 *   java -cp out/engine tools/Screenshots.java     then make the pictures
 *
 * Each game opens its window for a few seconds while this runs. Leave it alone until
 * it is done: the key presses are sent straight to the game, not through the keyboard.
 *
 * HOW IT WORKS
 * The main program runs this same file again once per game, as a separate Java
 * program, with the engine and that game on the class path (the games all have a class
 * called Main, so they cannot share one program). In that second program, a helper
 * thread starts the game's main(), then follows a "script": hold a key, wait some
 * updates, save a screenshot. Waiting is counted in updates (Engine.getUpdateCount()),
 * not in seconds, so the pictures come out the same on fast and slow computers.
 *
 * Screenshots are saved at the game's size (640x360) in out/screenshots/, then made
 * twice as big for the README. Every game pixel becomes a 2x2 square, so they stay
 * sharp when GitHub shows them.
 */
public class Screenshots {

    static final String RAW_FOLDER = "out/screenshots";
    static final String IMAGE_FOLDER = "docs/images";
    static final int README_SCALE = 2;

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            playGame(args[0], args[1]);    // we are the second program: play one game
            return;
        }
        if (!new File("out/engine/pge/Engine.class").isFile()) {
            System.out.println("Compile first: java Build.java");
            System.exit(1);
        }
        new File(RAW_FOLDER).mkdirs();
        new File(IMAGE_FOLDER).mkdirs();

        String[] games = {"demo", "platformer", "platformer-scenes", "starter"};
        for (int i = 0; i < games.length; i++) {
            startGame(games[i]);
        }

        String[] shots = {"demo", "platformer", "title", "level", "starter"};
        for (int i = 0; i < shots.length; i++) {
            enlarge(new File(RAW_FOLDER, shots[i] + ".png"), new File(IMAGE_FOLDER, shots[i] + ".png"));
        }
        makeHero();
        makeZoom();
        System.out.println("Pictures are in " + IMAGE_FOLDER + "/");
    }

    // ============================================================ the first program

    /** Runs this file again, as a separate program, for one game. */
    static void startGame(String gameName) throws IOException, InterruptedException {
        System.out.println("Playing '" + gameName + "'...");
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = new File("out/engine").getAbsolutePath()
                + File.pathSeparator + new File("out/games/" + gameName).getAbsolutePath();
        String thisFile = new File("tools/Screenshots.java").getAbsolutePath();
        String rawFolder = new File(RAW_FOLDER).getAbsolutePath();

        ProcessBuilder process = new ProcessBuilder(java, "-cp", classPath, thisFile, gameName, rawFolder);
        process.directory(new File("games/" + gameName));    // so the game finds its assets/
        process.inheritIO();
        int exitCode = process.start().waitFor();
        if (exitCode != 0) {
            System.out.println("'" + gameName + "' stopped with an error.");
            System.exit(1);
        }
    }

    /** Copies a picture, making every pixel a README_SCALE x README_SCALE square. */
    static void enlarge(File from, File to) throws IOException {
        BufferedImage small = ImageIO.read(from);
        BufferedImage big = new BufferedImage(small.getWidth() * README_SCALE,
                small.getHeight() * README_SCALE, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < big.getHeight(); y++) {
            for (int x = 0; x < big.getWidth(); x++) {
                big.setRGB(x, y, small.getRGB(x / README_SCALE, y / README_SCALE));
            }
        }
        ImageIO.write(big, "png", to);
    }

    /**
     * The banner at the top of the README, drawn with PGE's Canvas into our own array
     * of pixels (no window needed): a night sky, the title, and three screenshots.
     */
    static void makeHero() throws IOException {
        int width = 1280;
        int height = 520;
        int[] pixels = new int[width * height];
        Canvas canvas = new Canvas(width, height, pixels);

        // Sky: bands of colour from dark blue at the top to purple at the bottom.
        int bands = 13;
        for (int i = 0; i < bands; i++) {
            int top = i * height / bands;
            int bottom = (i + 1) * height / bands;
            canvas.fillRect(0, top, width, bottom - top, Color.rgb(16 + i * 3, 18 + i, 40 + i * 4));
        }
        // Stars, always in the same places (a fixed seed), so the banner does not
        // change every time it is made.
        java.util.Random random = new java.util.Random(7);
        for (int i = 0; i < 140; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int color = random.nextInt(4) == 0 ? Color.WHITE : Color.rgb(120, 130, 180);
            canvas.fillRect(x, y, 2, 2, color);
        }

        // Title and tagline, centered, with a hard drop shadow.
        String title = "PGE";
        int titleScale = 12;
        int titleX = (width - canvas.getTextWidth(title, titleScale)) / 2;
        canvas.drawText(title, titleX + 8, 40 + 8, Color.rgb(40, 20, 70), titleScale);
        canvas.drawText(title, titleX, 40, Color.YELLOW, titleScale);

        String tagline = "Pinto's Game Engine: 2D games in plain Java, every pixel drawn by hand";
        int taglineScale = 2;
        int taglineX = (width - canvas.getTextWidth(tagline, taglineScale)) / 2;
        canvas.drawText(tagline, taglineX, 160, Color.WHITE, taglineScale);

        // Three screenshots side by side, each with a frame. They are shown at their
        // real size, scaled down by half (one of every two pixels), to fit in a row.
        String[] shots = {"title", "level", "demo"};
        int shotWidth = 320;
        int shotHeight = 180;
        int gap = (width - shots.length * shotWidth) / (shots.length + 1);
        for (int i = 0; i < shots.length; i++) {
            Bitmap shot = halfSize(Bitmap.load(RAW_FOLDER + "/" + shots[i] + ".png"));
            int x = gap + i * (shotWidth + gap);
            int y = 240 - (i == 1 ? 16 : 0);    // the middle one sits a little higher
            canvas.fillRect(x - 4 + 6, y - 4 + 6, shotWidth + 8, shotHeight + 8, Color.rgb(10, 8, 24));
            canvas.fillRect(x - 4, y - 4, shotWidth + 8, shotHeight + 8, Color.WHITE);
            canvas.drawBitmap(shot, x, y);
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(image, "png", new File(IMAGE_FOLDER, "hero.png"));
    }

    /**
     * A close-up of the level's top-left corner (the coin counter and the hearts),
     * each game pixel made 6x6, so you can see the squares the picture is made of.
     */
    static void makeZoom() throws IOException {
        BufferedImage level = ImageIO.read(new File(RAW_FOLDER, "level.png"));
        int left = 0;
        int top = 0;
        int width = 118;
        int height = 30;
        int scale = 6;
        BufferedImage zoom = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < zoom.getHeight(); y++) {
            for (int x = 0; x < zoom.getWidth(); x++) {
                zoom.setRGB(x, y, level.getRGB(left + x / scale, top + y / scale));
            }
        }
        ImageIO.write(zoom, "png", new File(IMAGE_FOLDER, "zoom.png"));
    }

    /** A picture half as wide and half as tall: keeps one of every two pixels. */
    static Bitmap halfSize(Bitmap big) {
        Bitmap small = new Bitmap(big.getWidth() / 2, big.getHeight() / 2);
        for (int y = 0; y < small.getHeight(); y++) {
            for (int x = 0; x < small.getWidth(); x++) {
                small.setPixel(x, y, big.getPixel(x * 2, y * 2));
            }
        }
        return small;
    }

    // =========================================================== the second program

    private static Component gameSurface;
    private static String rawFolder;

    /** Starts the game on a helper thread's watch, then plays it from a script. */
    static void playGame(String gameName, String folder) throws Exception {
        rawFolder = folder;
        Thread player = new Thread() {
            @Override
            public void run() {
                try {
                    followScript(gameName);
                } catch (Exception problem) {
                    problem.printStackTrace();
                    System.exit(1);
                }
            }
        };
        player.start();
        // The game's main() never returns: Engine.run ends the program when the game stops.
        Class.forName("Main").getMethod("main", String[].class).invoke(null, (Object) new String[0]);
    }

    static void followScript(String gameName) throws Exception {
        waitForWindow();
        Sound.setMasterVolume(0);    // no music while we take pictures
        waitUpdates(30);

        if (gameName.equals("demo")) {
            hold(KeyEvent.VK_RIGHT);
            hold(KeyEvent.VK_UP);
            waitUpdates(20);
            shot("demo");
        } else if (gameName.equals("platformer")) {
            hold(KeyEvent.VK_RIGHT);
            waitUpdates(70);
            tap(KeyEvent.VK_SPACE, 14);
            waitUpdates(12);
            shot("platformer");
        } else if (gameName.equals("starter")) {
            for (int i = 0; i < 5; i++) {
                tap(KeyEvent.VK_SPACE, 3);
            }
            hold(KeyEvent.VK_RIGHT);
            hold(KeyEvent.VK_UP);
            waitUpdates(40);
            shot("starter");
        } else if (gameName.equals("platformer-scenes")) {
            waitUpdates(30);
            shot("title");
            tap(KeyEvent.VK_ENTER, 2);
            waitUpdates(30);
            // Run right, jumping every 20 updates, until the brick castle is on screen.
            hold(KeyEvent.VK_RIGHT);
            for (int i = 0; i < 11; i++) {
                waitUpdates(20);
                tap(KeyEvent.VK_SPACE, 16);
                waitUpdates(4);
            }
            shot("level");
        }
        Engine.stop();
    }

    /** Waits until the game has opened its window, and finds the area it draws in. */
    static void waitForWindow() throws InterruptedException {
        while (gameSurface == null) {
            Thread.sleep(50);
            Frame[] frames = Frame.getFrames();
            for (int i = 0; i < frames.length; i++) {
                Component found = findCanvas(frames[i]);
                if (found != null && Engine.getUpdateCount() > 0) {
                    gameSurface = found;
                }
            }
        }
    }

    /** Engine.Window draws into a java.awt.Canvas; the game's Input listens to it. */
    static Component findCanvas(Container container) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (child instanceof java.awt.Canvas) {
                return child;
            }
            if (child instanceof Container) {
                Component found = findCanvas((Container) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    static void waitUpdates(int count) throws InterruptedException {
        long target = Engine.getUpdateCount() + count;
        while (Engine.getUpdateCount() < target) {
            Thread.sleep(2);
        }
    }

    static void hold(int key) {
        sendKey(KeyEvent.KEY_PRESSED, key);
    }

    static void release(int key) {
        sendKey(KeyEvent.KEY_RELEASED, key);
    }

    /** Presses a key, keeps it down for some updates, and lets go. */
    static void tap(int key, int updates) throws InterruptedException {
        hold(key);
        waitUpdates(updates);
        release(key);
        waitUpdates(2);
    }

    static void sendKey(int type, int key) {
        KeyEvent event = new KeyEvent(gameSurface, type, System.currentTimeMillis(), 0, key,
                KeyEvent.CHAR_UNDEFINED);
        gameSurface.dispatchEvent(event);
    }

    /** Asks the engine for a screenshot and waits until it has been written. */
    static void shot(String name) throws InterruptedException {
        File file = new File(rawFolder, name + ".png");
        file.delete();
        Engine.saveScreenshot(file.getPath());
        while (!file.isFile()) {
            Thread.sleep(10);
        }
        waitUpdates(2);
    }
}
