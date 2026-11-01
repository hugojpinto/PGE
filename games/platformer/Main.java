import java.util.ArrayList;

import pge.Engine;
import pge.Game;
import pge.gfx.Animation;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.gfx.SpriteSheet;
import pge.input.Input;
import pge.input.Key;
import pge.world.Camera;
import pge.world.TileMap;

/**
 * A small platformer: run, jump, and collect every coin in the level.
 *
 *   Left / Right (or A / D)        run
 *   Space, Up or W                 jump (hold for a higher jump)
 *   R                              restart
 *   Escape                         quit
 *
 * The level is made of two tile maps loaded from CSV files: a background layer
 * (walls behind the castle) and the main layer (everything you can stand on).
 */
public class Main implements Game {

    // Tile numbers in assets/tiles.png
    static final int GRASS = 0;
    static final int DIRT = 1;
    static final int BRICK = 2;
    static final int STONE = 3;
    static final int COIN_MARKER = 7;

    // How the hero moves. Speeds are in pixels per update. GRAVITY is how much the
    // falling speed grows every update: a speed that keeps growing is an acceleration,
    // which is exactly what gravity is in real life. Change these numbers to change how
    // the game feels; tuning them is a big part of making a platformer fun.
    static final double RUN_SPEED = 2.0;
    static final double JUMP_SPEED = -7.5;      // negative is up; reaches about 4.5 tiles high
    static final double SHORT_JUMP_SPEED = -2.5;
    static final double GRAVITY = 0.35;
    // Without a limit, a long fall would get faster and faster until the hero moved
    // through several tiles per update. Real falling objects also stop speeding up at
    // some point, because of air resistance; that top speed is called "terminal velocity".
    static final double MAX_FALL_SPEED = 6.0;

    // The hero's picture is 16x16, but its body is smaller. This box (the "hitbox") is
    // what touches walls and coins, measured from the picture's top-left corner.
    static final int HITBOX_LEFT = 3;
    static final int HITBOX_TOP = 3;
    static final int HITBOX_WIDTH = 10;
    static final int HITBOX_HEIGHT = 13;

    static final int START_X = 32;
    static final int START_Y = 368;

    TileMap level;
    TileMap background;
    Camera camera;

    SpriteSheet sprites;
    Animation heroWalk;
    Animation coinSpin;

    double heroX;
    double heroY;
    double heroSpeedY = 0;
    boolean heroOnGround = false;
    boolean heroFacingLeft = false;
    boolean heroRunning = false;

    ArrayList<Coin> coins = new ArrayList<>();
    int coinsCollected = 0;

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360, "PGE Platformer");
    }

    @Override
    public void init() {
        SpriteSheet tiles = SpriteSheet.load("assets/tiles.png", 16, 16);
        background = TileMap.load("assets/level1-back.csv", tiles);
        sprites = SpriteSheet.load("assets/sprites.png", 16, 16);
        heroWalk = new Animation(sprites.getFrames(0, 4), 6);
        coinSpin = new Animation(sprites.getFrames(8, 4), 7);

        camera = new Camera(Engine.getWidth(), Engine.getHeight());
        camera.setSmoothness(0.15);

        restart();
    }

    /** Loads the level fresh, so collected coins come back, and puts the hero at the start. */
    void restart() {
        SpriteSheet tiles = background.getTileSet();
        level = TileMap.load("assets/level1.csv", tiles);
        level.setSolid(GRASS, true);
        level.setSolid(DIRT, true);
        level.setSolid(BRICK, true);
        level.setSolid(STONE, true);

        // The level designer marked coin spots with a coin tile. Turn each one into a
        // real Coin (which can spin and be collected) and clear the tile.
        coins.clear();
        coinsCollected = 0;
        for (int row = 0; row < level.getRows(); row++) {
            for (int column = 0; column < level.getColumns(); column++) {
                if (level.getTile(column, row) == COIN_MARKER) {
                    coins.add(new Coin(column * level.getTileWidth(), row * level.getTileHeight()));
                    level.setTile(column, row, TileMap.EMPTY);
                }
            }
        }

        heroX = START_X;
        heroY = START_Y;
        heroSpeedY = 0;
        camera.setLimits(level.getWidth(), level.getHeight());
        camera.centerOn(heroX + 8, heroY + 8);
    }

    // =================================================================== UPDATE

    @Override
    public void update() {
        if (Input.wasPressed(Key.R)) {
            restart();
        }
        if (Input.wasPressed(Key.ESCAPE)) {
            Engine.stop();
        }

        // ---- Running
        double runDistance = 0;
        if (Input.isDown(Key.LEFT) || Input.isDown(Key.A)) {
            runDistance = -RUN_SPEED;
            heroFacingLeft = true;
        }
        if (Input.isDown(Key.RIGHT) || Input.isDown(Key.D)) {
            runDistance = RUN_SPEED;
            heroFacingLeft = false;
        }
        heroRunning = runDistance != 0;
        moveHorizontally(runDistance);

        // ---- Jumping: only from the ground. Letting go early makes a shorter jump.
        // To check for ground, we ask whether the hero would hit a wall one pixel lower.
        // The short jump works by cutting the upward speed when the key is let go.
        // Super Mario Bros. (1985) made this "hold to jump higher" control famous,
        // and most platformers since have worked this way.
        heroOnGround = hitsWall(heroX, heroY + 1);
        boolean jumpHeld = Input.isDown(Key.SPACE) || Input.isDown(Key.UP) || Input.isDown(Key.W);
        boolean jumpPressed = Input.wasPressed(Key.SPACE) || Input.wasPressed(Key.UP) || Input.wasPressed(Key.W);
        if (heroOnGround && jumpPressed) {
            heroSpeedY = JUMP_SPEED;
        }
        if (!jumpHeld && heroSpeedY < SHORT_JUMP_SPEED) {
            heroSpeedY = SHORT_JUMP_SPEED;
        }

        // ---- Falling: gravity makes the falling speed a little bigger every update
        heroSpeedY = Math.min(heroSpeedY + GRAVITY, MAX_FALL_SPEED);
        moveVertically(heroSpeedY);

        // ---- Animation: walk while running on the ground, otherwise stand still
        if (heroRunning && heroOnGround) {
            heroWalk.update();
        } else {
            heroWalk.restart();
        }
        coinSpin.update();

        // ---- Coins
        for (Coin coin : coins) {
            if (!coin.isCollected() && touchesHero(coin)) {
                coin.collect();
                coinsCollected = coinsCollected + 1;
            }
        }

        camera.follow(heroX + 8, heroY + 8);
    }

    /**
     * Moves the hero left or right, but never into a wall.
     *
     * We move at most one pixel at a time and check for a wall before each step.
     * That way the hero always stops exactly against the wall, even at high speed.
     * Moving the whole distance at once and then checking could carry the hero into
     * the wall, or even through a thin wall to the other side.
     *
     * Moving left-right and up-down separately (in two methods) matters too: when the
     * hero runs into a wall while falling, the sideways move stops but the fall goes on,
     * so the hero slides down the wall instead of sticking to it.
     */
    void moveHorizontally(double distance) {
        double remaining = Math.abs(distance);
        double direction = Math.signum(distance);
        while (remaining > 0) {
            double step = Math.min(1, remaining);
            double nextX = heroX + direction * step;
            if (hitsWall(nextX, heroY)) {
                heroX = Math.floor(heroX);    // rest on a whole pixel, right against the wall
                return;
            }
            heroX = nextX;
            remaining = remaining - step;
        }
    }

    /** Moves the hero up or down, one pixel at a time, stopping at floors and ceilings. */
    void moveVertically(double distance) {
        double remaining = Math.abs(distance);
        double direction = Math.signum(distance);
        while (remaining > 0) {
            double step = Math.min(1, remaining);
            double nextY = heroY + direction * step;
            if (hitsWall(heroX, nextY)) {
                heroSpeedY = 0;               // landed on a floor, or bumped a ceiling
                heroY = Math.floor(heroY);    // rest on a whole pixel
                return;
            }
            heroY = nextY;
            remaining = remaining - step;
        }
    }

    /** Would the hero's hitbox be inside a wall if the hero stood at (x, y)? */
    boolean hitsWall(double x, double y) {
        int left = (int) Math.floor(x) + HITBOX_LEFT;
        int top = (int) Math.floor(y) + HITBOX_TOP;
        return level.overlapsSolid(left, top, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    /** Does the hero's hitbox overlap the middle part of this coin? */
    boolean touchesHero(Coin coin) {
        int heroLeft = (int) heroX + HITBOX_LEFT;
        int heroTop = (int) heroY + HITBOX_TOP;
        int coinLeft = coin.getX() + 3;
        int coinTop = coin.getY() + 2;
        int coinSize = 10;
        return heroLeft < coinLeft + coinSize && coinLeft < heroLeft + HITBOX_WIDTH
                && heroTop < coinTop + coinSize && coinTop < heroTop + HITBOX_HEIGHT;
    }

    // ===================================================================== DRAW

    @Override
    public void draw(Canvas canvas) {
        canvas.clear(Color.rgb(110, 170, 230));    // sky

        drawHills(canvas);

        // ---- The world, as seen by the camera
        camera.applyTo(canvas);
        background.draw(canvas);
        level.draw(canvas);
        for (Coin coin : coins) {
            if (!coin.isCollected()) {
                canvas.drawBitmap(coinSpin.getFrame(), coin.getX(), coin.getY());
            }
        }
        if (heroOnGround) {
            canvas.drawBitmap(heroWalk.getFrame(), (int) heroX, (int) heroY, heroFacingLeft, false);
        } else {
            canvas.drawBitmap(sprites.get(1), (int) heroX, (int) heroY, heroFacingLeft, false);   // mid-stride pose for jumps
        }

        // ---- The screen: score and help
        canvas.resetCamera();
        drawScore(canvas);
        canvas.drawText("Arrows: run   Space: jump   R: restart", 6, canvas.getHeight() - 12, Color.WHITE);
        if (coinsCollected == coins.size()) {
            drawCentered(canvas, "ALL COINS!", 120, Color.YELLOW, 4);
            drawCentered(canvas, "Press R to play again", 160, Color.WHITE, 1);
        }
    }

    /**
     * Two rows of hills that scroll slower than the level, so they seem far away.
     *
     * Looking out of a train window, nearby trees rush past while distant mountains
     * hardly move. That difference is called "parallax", and it is one of the ways our
     * eyes judge distance. Games copy it to make a flat picture feel deep: here the far
     * row of hills moves at 1/4 of the camera's speed and the near row at 1/2. Arcade
     * games of the early 1980s, such as Moon Patrol (1982), were among the first to
     * use this "parallax scrolling".
     */
    void drawHills(Canvas canvas) {
        canvas.setCamera(camera.getX() / 4, camera.getY() / 4);
        for (int x = -40; x < 1400; x = x + 150) {
            canvas.fillCircle(x, 330, 110, Color.rgb(140, 190, 225));
        }
        canvas.setCamera(camera.getX() / 2, camera.getY() / 2);
        for (int x = 0; x < 1400; x = x + 110) {
            canvas.fillCircle(x, 360, 80, Color.rgb(90, 160, 110));
        }
    }

    void drawScore(Canvas canvas) {
        String text = coinsCollected + " / " + coins.size();
        int boxWidth = 28 + canvas.getTextWidth(text);
        canvas.fillRect(4, 4, boxWidth, 24, Color.BLACK);
        canvas.drawRect(4, 4, boxWidth, 24, Color.WHITE);
        canvas.drawBitmap(coinSpin.getFrame(), 8, 8);
        canvas.drawText(text, 26, 12, Color.WHITE);
    }

    /** Text centred left-to-right, with a dark shadow. */
    void drawCentered(Canvas canvas, String text, int y, int color, int scale) {
        int x = (canvas.getWidth() - canvas.getTextWidth(text, scale)) / 2;
        canvas.drawText(text, x + scale, y + scale, Color.BLACK, scale);
        canvas.drawText(text, x, y, color, scale);
    }
}
