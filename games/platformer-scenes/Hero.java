import pge.gfx.Animation;
import pge.gfx.Bitmap;
import pge.gfx.SpriteSheet;
import pge.input.Input;
import pge.input.Key;
import pge.scene.GameObject;
import pge.world.TileMap;

/**
 * The player. Runs, jumps, stops at walls, stomps slimes, and gets hurt by them.
 *
 * Compare with games/platformer/Main.java: the movement code is the same, but here
 * it lives in the hero itself instead of in the game.
 */
public class Hero extends GameObject {

    // How the hero moves, in pixels per update
    static final double RUN_SPEED = 2.0;
    static final double JUMP_SPEED = -7.5;        // negative is up
    static final double SHORT_JUMP_SPEED = -2.5;
    static final double STOMP_BOUNCE_SPEED = -5.0;
    static final double HURT_BOUNCE_SPEED = -4.0;
    static final double GRAVITY = 0.35;
    static final double MAX_FALL_SPEED = 6.0;
    static final int SAFE_UPDATES_AFTER_HURT = 90;    // 1.5 seconds

    private final Animation walk;
    private final Bitmap standing;
    private final Bitmap jumping;

    private double speedY = 0;
    private boolean onGround = false;
    private int safeTimer = 0;    // while above 0, slimes cannot hurt the hero

    public Hero(double x, double y, SpriteSheet sprites) {
        super(x, y);
        setName("hero");
        setDepth(10);                    // drawn in front of coins and slimes
        setHitbox(3, 3, 10, 13);
        walk = new Animation(sprites.getFrames(0, 4), 6);
        standing = sprites.get(0);
        jumping = sprites.get(1);
        setSprite(standing);
    }

    @Override
    public void update() {
        // ---- Running
        double runDistance = 0;
        if (Input.isDown(Key.LEFT) || Input.isDown(Key.A)) {
            runDistance = -RUN_SPEED;
            setFlipX(true);
        }
        if (Input.isDown(Key.RIGHT) || Input.isDown(Key.D)) {
            runDistance = RUN_SPEED;
            setFlipX(false);
        }
        moveHorizontally(runDistance);

        // ---- Jumping: only from the ground. Letting go early makes a shorter jump.
        onGround = hitsWall(getX(), getY() + 1);
        boolean jumpHeld = Input.isDown(Key.SPACE) || Input.isDown(Key.UP) || Input.isDown(Key.W);
        boolean jumpPressed = Input.wasPressed(Key.SPACE) || Input.wasPressed(Key.UP) || Input.wasPressed(Key.W);
        if (onGround && jumpPressed) {
            speedY = JUMP_SPEED;
        }
        if (!jumpHeld && speedY < SHORT_JUMP_SPEED) {
            speedY = SHORT_JUMP_SPEED;
        }

        // ---- Falling
        speedY = Math.min(speedY + GRAVITY, MAX_FALL_SPEED);
        moveVertically(speedY);

        // ---- Slimes
        if (safeTimer > 0) {
            safeTimer = safeTimer - 1;
        }
        for (GameObject slime : getScene().findAll("slime")) {
            if (overlaps(slime)) {
                touchSlime(slime);
            }
        }

        // ---- Picture: walk while running on the ground, otherwise a still pose
        if (runDistance != 0 && onGround) {
            setAnimation(walk);
        } else {
            setAnimation(null);
            if (onGround) {
                setSprite(standing);
            } else {
                setSprite(jumping);
            }
        }
        // Blink while safe after being hurt: visible for 4 updates, hidden for 4...
        setVisible(safeTimer == 0 || (safeTimer / 4) % 2 == 0);
    }

    /** Landing on top of a slime squashes it. Touching it any other way hurts. */
    private void touchSlime(GameObject slime) {
        int myBottom = getHitboxY() + getHitboxHeight();
        int slimeMiddle = slime.getHitboxY() + slime.getHitboxHeight() / 2;
        if (speedY > 0 && myBottom <= slimeMiddle) {
            slime.remove();
            speedY = STOMP_BOUNCE_SPEED;
        } else if (safeTimer == 0) {
            safeTimer = SAFE_UPDATES_AFTER_HURT;
            speedY = HURT_BOUNCE_SPEED;
            LevelScene level = (LevelScene) getScene();
            level.heroHurt();
        }
    }

    // ================================================================== walls

    /** Moves left or right one pixel at a time, stopping at walls. */
    private void moveHorizontally(double distance) {
        double remaining = Math.abs(distance);
        double direction = Math.signum(distance);
        while (remaining > 0) {
            double step = Math.min(1, remaining);
            double nextX = getX() + direction * step;
            if (hitsWall(nextX, getY())) {
                setX(Math.floor(getX()));    // rest on a whole pixel, right against the wall
                return;
            }
            setX(nextX);
            remaining = remaining - step;
        }
    }

    /** Moves up or down one pixel at a time, stopping at floors and ceilings. */
    private void moveVertically(double distance) {
        double remaining = Math.abs(distance);
        double direction = Math.signum(distance);
        while (remaining > 0) {
            double step = Math.min(1, remaining);
            double nextY = getY() + direction * step;
            if (hitsWall(getX(), nextY)) {
                speedY = 0;                   // landed on a floor, or bumped a ceiling
                setY(Math.floor(getY()));     // rest on a whole pixel
                return;
            }
            setY(nextY);
            remaining = remaining - step;
        }
    }

    /** Would the hitbox be inside a wall if the hero stood at (x, y)? */
    private boolean hitsWall(double x, double y) {
        TileMap map = ((LevelScene) getScene()).getLevel();
        int left = (int) Math.floor(x) + getHitboxOffsetX();
        int top = (int) Math.floor(y) + getHitboxOffsetY();
        return map.overlapsSolid(left, top, getHitboxWidth(), getHitboxHeight());
    }
}
