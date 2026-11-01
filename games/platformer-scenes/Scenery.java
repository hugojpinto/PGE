import pge.gfx.Canvas;
import pge.gfx.Color;

/**
 * The sky and the hills behind the level. Every scene uses the same background,
 * so it lives here, in one place.
 */
public class Scenery {

    public static final int SKY = Color.rgb(110, 170, 230);

    /**
     * Two rows of hills that scroll slower than the level, so they seem far away
     * (parallax). cameraX and cameraY are where the level camera is; the far hills
     * move at 1/4 of its speed, the near ones at 1/2.
     */
    public static void draw(Canvas canvas, int cameraX, int cameraY) {
        canvas.resetCamera();
        canvas.clear(SKY);
        canvas.setCamera(cameraX / 4, cameraY / 4);
        for (int x = -40; x < 1400; x = x + 150) {
            canvas.fillCircle(x, 330, 110, Color.rgb(140, 190, 225));
        }
        canvas.setCamera(cameraX / 2, cameraY / 2);
        for (int x = 0; x < 1400; x = x + 110) {
            canvas.fillCircle(x, 360, 80, Color.rgb(90, 160, 110));
        }
        canvas.resetCamera();
    }
}
