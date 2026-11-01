package pge.scene;

import pge.Engine;
import pge.Game;
import pge.gfx.Canvas;

/**
 * Runs scenes using the plain Game interface: it is the bridge between the two ways
 * of making a game with PGE. Engine.run(scene, ...) wraps your scene in one of these.
 *
 * It shows that scenes are not magic: they are a Game like any other, whose
 * update() and draw() happen to update and draw a list of objects.
 */
public class SceneRunner implements Game {

    private int width;
    private int height;
    private Scene current;
    private Scene next = null;

    public SceneRunner(Scene firstScene) {
        current = firstScene;
    }

    /** Asks to switch to another scene. The switch happens at the start of the next update. */
    public void changeScene(Scene scene) {
        next = scene;
    }

    public Scene getCurrentScene() {
        return current;
    }

    @Override
    public void init() {
        // Engine.run created the window before calling init(), so its size is known now.
        width = Engine.getWidth();
        height = Engine.getHeight();
        begin(current);
    }

    @Override
    public void update() {
        if (next != null) {
            current = next;
            next = null;
            begin(current);
        }
        current.runUpdate();
    }

    @Override
    public void draw(Canvas canvas) {
        current.runDraw(canvas);
    }

    private void begin(Scene scene) {
        scene.setUp(width, height);
        scene.start();
        scene.removeAndJoin();    // objects added in start() join right away
    }
}
