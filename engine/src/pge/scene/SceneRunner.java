package pge.scene;

import pge.Engine;
import pge.Game;
import pge.gfx.Canvas;

/**
 * Runs scenes through the plain Game interface. It connects the two ways of making a
 * game with PGE: Engine.run(scene, ...) wraps your scene in one of these.
 *
 * The engine only knows how to run a Game. A Scene is not a Game: it has different
 * methods (start, runUpdate, runDraw). This class is a Game whose init(), update() and
 * draw() simply call the current scene's methods. So, to the engine, a game made of
 * scenes is a Game like any other, and the engine did not need to change at all to
 * support scenes.
 *
 * A class that lets one kind of object be used where another kind is expected is
 * called an "adapter", like a travel plug adapter that lets a plug fit a foreign
 * socket. It is one of the design patterns described in the book "Design Patterns"
 * (1994) by Gamma, Helm, Johnson and Vlissides, one of the best-known books about
 * object-oriented programming.
 *
 * It also takes care of switching scenes, which only ever happens between updates,
 * never in the middle of one.
 */
public class SceneRunner implements Game {

    private int width;
    private int height;
    private Scene current;
    private Scene next = null;

    public SceneRunner(Scene firstScene) {
        current = firstScene;
    }

    /**
     * Asks to switch to another scene. The switch happens at the start of the next
     * update. If we switched straight away, the rest of the current update would run
     * half in the old scene and half in the new one.
     */
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

    /** Prepares a scene, calls its start(), and lets the objects it added join straight away. */
    private void begin(Scene scene) {
        scene.setUp(width, height);
        scene.start();
        scene.removeAndJoin();    // objects added in start() join right away
    }
}
