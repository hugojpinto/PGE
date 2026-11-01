/**
 * A coin somewhere in the level. It knows where it is and whether it was collected.
 *
 * This class only holds data: it has no behaviour of its own. The game's Main class
 * checks the coins and draws them. Compare it with games/platformer-scenes/Coin.java,
 * where the coin is a GameObject that checks for the hero itself.
 */
public class Coin {

    private final int x;
    private final int y;
    private boolean collected = false;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
