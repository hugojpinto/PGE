package pge.ui;

import pge.gfx.Bitmap;
import pge.gfx.Canvas;

/**
 * A bitmap on the screen, like a heart for each life or an icon next to the score.
 *
 *   Picture heart = new Picture(sheet.get(12), 4, 4);
 *   addUI(heart);
 */
public class Picture extends UIElement {

    private Bitmap bitmap;

    public Picture(Bitmap bitmap, int x, int y) {
        super(x, y, bitmap.getWidth(), bitmap.getHeight());
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getX(), getY());
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        setSize(bitmap.getWidth(), bitmap.getHeight());
    }
}
