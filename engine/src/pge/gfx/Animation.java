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
 * At 60 updates per second, 8 updates per frame means 60 / 8 = 7.5 pictures per
 * second. Smaller numbers make the animation faster. Pixel art animations are usually
 * slow like this, with only a few frames: a walk often has just 4. Our eyes fill in
 * the movement between them. Cartoons work the same way; hand-drawn animation often
 * shows each drawing for two or three film frames.
 *
 * By default an animation loops forever. Call setLooping(false) for one that plays
 * once and stops on its last frame (an explosion, for example), then use isFinished().
 *
 * HOW IT WORKS
 * The animation does not measure time. It counts how many times update() was called,
 * and works out the frame from that count (see getFrameIndex). Because the engine
 * calls update() exactly 60 times per second, counting updates is the same as
 * measuring time, and it stays in step with the rest of the game even if the
 * computer is slow.
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

    /**
     * The number (0, 1, 2...) of the frame to draw right now.
     *
     * Integer division does the work. With 8 updates per frame, updates 0 to 7 give
     * frame 0, updates 8 to 15 give frame 1, and so on. For a looping animation, the
     * remainder operator % wraps the number around: with 4 frames, frame 4 becomes 0
     * again, frame 5 becomes 1, and so on. A non-looping animation stays on its last frame.
     */
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
