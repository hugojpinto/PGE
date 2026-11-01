import pge.gfx.Animation;
import pge.gfx.SpriteSheet;
import pge.scene.GameObject;
import pge.world.TileMap;

/**
 * A slime that wobbles back and forth. It turns around at walls and at the edge of
 * whatever it stands on, so it never falls off.
 *
 * This is about the simplest enemy behaviour (game makers call it "AI", even when it
 * is this simple). Classic platformers are full of enemies like this; the red Koopa
 * Troopas in Super Mario Bros., for example, turn around at the edge of a platform
 * instead of walking off it. Simple, predictable enemies are fair: the player can
 * watch them and learn when to jump.
 */
public class Slime extends GameObject {

    static final double SPEED = 0.5;

    private int direction = 1;    // 1 = right, -1 = left

    public Slime(double x, double y, SpriteSheet sprites) {
        super(x, y);
        setName("slime");
        setDepth(5);
        setHitbox(2, 8, 12, 8);     // the dome is lower and narrower than its 16x16 picture
        setAnimation(new Animation(sprites.getFrames(4, 2), 20));
    }

    @Override
    public void update() {
        TileMap map = ((LevelScene) getScene()).getLevel();
        double nextX = getX() + direction * SPEED;

        // Is there a wall where the hitbox would be?
        int left = (int) Math.floor(nextX) + getHitboxOffsetX();
        boolean wallAhead = map.overlapsSolid(left, getHitboxY(), getHitboxWidth(), getHitboxHeight());

        // Is there floor under the front edge? Look one pixel below the hitbox,
        // just past the side we are walking toward.
        int frontX = left - 1;
        if (direction > 0) {
            frontX = left + getHitboxWidth();
        }
        int belowY = getHitboxY() + getHitboxHeight();
        boolean floorAhead = map.isSolidAt(frontX, belowY);

        if (wallAhead || !floorAhead) {
            direction = -direction;
        } else {
            setX(nextX);
        }
    }
}
