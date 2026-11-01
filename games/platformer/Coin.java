/**
 * A coin somewhere in the level. It knows where it is and whether it was collected.
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
