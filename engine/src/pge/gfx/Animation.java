package pge.gfx;

/**
 * A list of frames shown one after the other, like a flip book.
 *
 *   Animation walk = new Animation(sheet.getFrames(0, 4), 8);   // 4 frames, each shown for 8 updates
 *
 *   // in update():
 *   walk.update();
 *
 *   // in draw():
 *   canvas.drawBitmap(walk.getFrame(), x, y);
 *
 * At 60 updates per second, 8 updates per frame means 7.5 frames per second.
 * Smaller numbers make the animation faster.
 *
 * By default an animation loops forever. Call setLooping(false) for one that plays
 * once and stops on its last frame (an explosion, for example), then use isFinished().
 */
public class Animation {

    private final Bitmap[] frames;
    private final int updatesPerFrame;
    private boolean looping = true;
    private int updates = 0;    // how many times update() was called since the start

    public Animation(Bitmap[] frames, int updatesPerFrame) {
        if (frames.length == 0) {
            throw new IllegalArgumentException("An animation needs at least one frame");
        }
        if (updatesPerFrame <= 0) {
            throw new IllegalArgumentException("updatesPerFrame must be at least 1, got " + updatesPerFrame);
        }
        this.frames = frames;
        this.updatesPerFrame = updatesPerFrame;
    }

    /** Moves the animation forward by one update. Call it once in every update(). */
    public void update() {
        if (!isFinished()) {
            updates = updates + 1;
        }
    }

    /** The picture to draw right now. */
    public Bitmap getFrame() {
        return frames[getFrameIndex()];
    }

    /** The number (0, 1, 2...) of the frame to draw right now. */
    public int getFrameIndex() {
        int index = updates / updatesPerFrame;
        if (looping) {
            return index % frames.length;
        }
        return Math.min(index, frames.length - 1);
    }

    /** Back to the first frame. */
    public void restart() {
        updates = 0;
    }

    /** True when a non-looping animation has reached its end. Looping animations never finish. */
    public boolean isFinished() {
        return !looping && updates >= frames.length * updatesPerFrame;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public int getFrameCount() {
        return frames.length;
    }
}
