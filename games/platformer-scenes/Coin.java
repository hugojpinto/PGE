import pge.gfx.Animation;
import pge.gfx.SpriteSheet;
import pge.scene.GameObject;

/**
 * A spinning coin. When the hero touches it, it tells the level and disappears.
 *
 * Compare with games/platformer/Coin.java: there a coin only stores data and the
 * game checks it; here the coin checks for the hero itself.
 */
public class Coin extends GameObject {

    public Coin(double x, double y, SpriteSheet sprites) {
        super(x, y);
        setName("coin");
        setHitbox(3, 2, 10, 12);
        setAnimation(new Animation(sprites.getFrames(8, 4), 7));
    }

    @Override
    public void update() {
        LevelScene level = (LevelScene) getScene();
        if (overlaps(level.getHero())) {
            remove();
            Sounds.coin.play();
            level.coinCollected();
        }
    }
}
