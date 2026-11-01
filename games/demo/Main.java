import pge.Engine;
import pge.Game;
import pge.audio.Sound;
import pge.audio.Synth;
import pge.gfx.Animation;
import pge.gfx.Bitmap;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.gfx.SpriteSheet;
import pge.input.Input;
import pge.input.Key;
import pge.world.Camera;

/**
 * PGE demo: walk around a world bigger than the screen and collect every coin.
 *
 *   Arrows or WASD   move
 *   Mouse click      jump the hero to that spot
 *   Space            flash
 *   R                restart
 *   Escape           quit
 *   F11 / Alt+Enter  fullscreen
 *   F3               speed statistics
 */
public class Main implements Game {

    static final int WORLD_WIDTH = 2000;
    static final int WORLD_HEIGHT = 1200;
    static final int GRID_SIZE = 100;
    static final int STAR_COUNT = 400;
    static final int COIN_COUNT = 30;
    static final int SPRITE_SIZE = 16;
    static final int FLASH_UPDATES = 12;

    // Pictures
    SpriteSheet sheet;
    Animation heroWalk;
    Animation coinSpin;
    Bitmap heart;
    Camera camera;

    // Sounds, made with code instead of loaded from files
    Sound coinSound;
    Sound teleportSound;
    Sound winSound;

    // The hero
    double heroX;
    double heroY;
    double heroSpeed = 2.5;
    boolean heroFacingLeft = false;
    boolean heroMoving = false;
    int flashTimer = 0;

    // The coins: position, and whether it was already collected
    int[] coinX = new int[COIN_COUNT];
    int[] coinY = new int[COIN_COUNT];
    boolean[] coinTaken = new boolean[COIN_COUNT];
    int coinsCollected = 0;

    // Background stars
    int[] starX = new int[STAR_COUNT];
    int[] starY = new int[STAR_COUNT];

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360, "PGE Demo");
    }

    @Override
    public void init() {
        sheet = SpriteSheet.load("assets/sprites.png", SPRITE_SIZE, SPRITE_SIZE);
        heroWalk = new Animation(sheet.getFrames(0, 4), 8);
        coinSpin = new Animation(sheet.getFrames(8, 4), 7);
        heart = sheet.get(12);

        coinSound = Sound.fromSamples(Synth.join(
                Synth.note(Synth.SQUARE, "B5", 0.05, 0.25),
                Synth.note(Synth.SQUARE, "E6", 0.18, 0.25)));
        teleportSound = Sound.fromSamples(Synth.tone(Synth.SINE, 300, 1200, 0.2, 0.4));
        winSound = Sound.fromSamples(Synth.join(Synth.join(
                Synth.note(Synth.TRIANGLE, "C5", 0.12, 0.5),
                Synth.note(Synth.TRIANGLE, "E5", 0.12, 0.5)),
                Synth.note(Synth.TRIANGLE, "G5", 0.5, 0.5)));

        camera = new Camera(Engine.getWidth(), Engine.getHeight());
        camera.setLimits(WORLD_WIDTH, WORLD_HEIGHT);

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = randomBetween(0, WORLD_WIDTH);
            starY[i] = randomBetween(0, WORLD_HEIGHT);
        }
        restart();
    }

    /** Puts the hero back in the start position and scatters the coins again. */
    void restart() {
        heroX = 300;
        heroY = 200;
        camera.centerOn(heroX + SPRITE_SIZE / 2, heroY + SPRITE_SIZE / 2);
        coinsCollected = 0;
        for (int i = 0; i < COIN_COUNT; i++) {
            coinX[i] = randomBetween(40, WORLD_WIDTH - 40);
            coinY[i] = randomBetween(40, WORLD_HEIGHT - 40);
            coinTaken[i] = false;
        }
    }

    @Override
    public void update() {
        // ---- Movement
        heroMoving = false;
        if (Input.isDown(Key.LEFT) || Input.isDown(Key.A)) {
            heroX = heroX - heroSpeed;
            heroFacingLeft = true;
            heroMoving = true;
        }
        if (Input.isDown(Key.RIGHT) || Input.isDown(Key.D)) {
            heroX = heroX + heroSpeed;
            heroFacingLeft = false;
            heroMoving = true;
        }
        if (Input.isDown(Key.UP) || Input.isDown(Key.W)) {
            heroY = heroY - heroSpeed;
            heroMoving = true;
        }
        if (Input.isDown(Key.DOWN) || Input.isDown(Key.S)) {
            heroY = heroY + heroSpeed;
            heroMoving = true;
        }
        heroX = clamp(heroX, 0, WORLD_WIDTH - SPRITE_SIZE);
        heroY = clamp(heroY, 0, WORLD_HEIGHT - SPRITE_SIZE);

        // Walk animation only while moving; standing still shows the first frame.
        if (heroMoving) {
            heroWalk.update();
        } else {
            heroWalk.restart();
        }
        coinSpin.update();

        // ---- Other keys
        if (Input.wasPressed(Key.SPACE)) {
            flashTimer = FLASH_UPDATES;
        }
        if (flashTimer > 0) {
            flashTimer = flashTimer - 1;
        }
        if (Input.wasPressed(Key.R)) {
            restart();
        }
        // The pointer is in screen pixels; the camera turns that into a world position.
        if (Input.wasPointerPressed() && Input.isPointerInside()) {
            heroX = camera.toWorldX(Input.getPointerX()) - SPRITE_SIZE / 2;
            heroY = camera.toWorldY(Input.getPointerY()) - SPRITE_SIZE / 2;
            heroX = clamp(heroX, 0, WORLD_WIDTH - SPRITE_SIZE);
            heroY = clamp(heroY, 0, WORLD_HEIGHT - SPRITE_SIZE);
            flashTimer = FLASH_UPDATES;
            teleportSound.play();
        }
        if (Input.wasPressed(Key.ESCAPE)) {
            Engine.stop();
        }

        // ---- Collecting coins: do the hero's and the coin's squares overlap?
        for (int i = 0; i < COIN_COUNT; i++) {
            if (!coinTaken[i] && overlaps(heroX, heroY, coinX[i], coinY[i])) {
                coinTaken[i] = true;
                coinsCollected = coinsCollected + 1;
                coinSound.play();
                if (coinsCollected == COIN_COUNT) {
                    winSound.play();
                }
            }
        }

        camera.follow(heroX + SPRITE_SIZE / 2, heroY + SPRITE_SIZE / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.clear(Color.DARK_BLUE);

        // ---- World space: the camera keeps the hero in the middle of the screen
        camera.applyTo(canvas);

        for (int i = 0; i < STAR_COUNT; i++) {
            canvas.setPixel(starX[i], starY[i], Color.GRAY);
        }
        for (int x = 0; x <= WORLD_WIDTH; x = x + GRID_SIZE) {
            canvas.drawLine(x, 0, x, WORLD_HEIGHT, Color.DARK_GRAY);
        }
        for (int y = 0; y <= WORLD_HEIGHT; y = y + GRID_SIZE) {
            canvas.drawLine(0, y, WORLD_WIDTH, y, Color.DARK_GRAY);
        }
        canvas.drawRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT, Color.RED);

        for (int i = 0; i < COIN_COUNT; i++) {
            if (!coinTaken[i]) {
                canvas.drawBitmap(coinSpin.getFrame(), coinX[i], coinY[i]);
            }
        }

        if (flashTimer > 0) {
            canvas.drawSilhouette(heroWalk.getFrame(), (int) heroX, (int) heroY, heroFacingLeft, Color.WHITE);
        } else {
            canvas.drawBitmap(heroWalk.getFrame(), (int) heroX, (int) heroY, heroFacingLeft, false);
        }

        // ---- Screen space: the camera is off, these stay put
        canvas.resetCamera();
        drawScore(canvas);
        drawMiniMap(canvas);
        canvas.drawText("Arrows: move   Click: jump there   Space: flash   R: restart", 6, canvas.getHeight() - 12, Color.GRAY);

        if (coinsCollected == COIN_COUNT) {
            drawCentered(canvas, "YOU WIN!", canvas.getHeight() / 2 - 24, Color.YELLOW, 4);
            drawCentered(canvas, "Press R to play again", canvas.getHeight() / 2 + 16, Color.WHITE, 1);
        }
    }

    /** Top-left: a box with a heart, a coin and the count. */
    void drawScore(Canvas canvas) {
        String text = coinsCollected + " / " + COIN_COUNT;
        int boxWidth = 46 + canvas.getTextWidth(text);
        canvas.fillRect(4, 4, boxWidth, 24, Color.BLACK);
        canvas.drawRect(4, 4, boxWidth, 24, Color.GRAY);
        canvas.drawBitmap(heart, 8, 8);
        canvas.drawBitmap(coinSpin.getFrame(), 26, 8);
        canvas.drawText(text, 44, 12, Color.WHITE);
    }

    /** Bottom-right: the whole world shrunk 20 times. */
    void drawMiniMap(Canvas canvas) {
        int shrink = 20;
        int mapWidth = WORLD_WIDTH / shrink;
        int mapHeight = WORLD_HEIGHT / shrink;
        int left = canvas.getWidth() - mapWidth - 6;
        int top = canvas.getHeight() - mapHeight - 6;

        canvas.fillRect(left, top, mapWidth, mapHeight, Color.BLACK);
        canvas.drawRect(left - 1, top - 1, mapWidth + 2, mapHeight + 2, Color.GRAY);
        for (int i = 0; i < COIN_COUNT; i++) {
            if (!coinTaken[i]) {
                canvas.setPixel(left + coinX[i] / shrink, top + coinY[i] / shrink, Color.YELLOW);
            }
        }
        canvas.fillRect(left + (int) heroX / shrink, top + (int) heroY / shrink, 2, 2, Color.CYAN);
    }

    /** Text centered left-to-right, with a dark shadow one step down and right. */
    void drawCentered(Canvas canvas, String text, int y, int color, int scale) {
        int x = (canvas.getWidth() - canvas.getTextWidth(text, scale)) / 2;
        canvas.drawText(text, x + scale, y + scale, Color.BLACK, scale);
        canvas.drawText(text, x, y, color, scale);
    }

    /** True if two 16x16 sprites at these positions overlap (with a little forgiveness). */
    static boolean overlaps(double ax, double ay, double bx, double by) {
        double margin = 3;
        return ax + margin < bx + SPRITE_SIZE - margin
                && bx + margin < ax + SPRITE_SIZE - margin
                && ay + margin < by + SPRITE_SIZE - margin
                && by + margin < ay + SPRITE_SIZE - margin;
    }

    static int randomBetween(int min, int max) {
        return min + (int) (Math.random() * (max - min));
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
