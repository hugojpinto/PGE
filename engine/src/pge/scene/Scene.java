package pge.scene;

import java.util.ArrayList;

import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.ui.UIElement;
import pge.world.Camera;
import pge.world.TileMap;

/**
 * One screen of a game: the title menu, a level, the game over screen...
 *
 * A scene holds everything on that screen and runs it for you:
 *   - tile map layers   (addLayer)  drawn first, behind everything
 *   - game objects      (add)       updated and drawn every frame, through the camera
 *   - UI elements       (addUI)     buttons, labels and boxes, drawn on top, not moved by the camera
 *
 * Make your own scene by extending this class:
 *
 *   public class Level1 extends Scene {
 *       @Override
 *       public void start() {
 *           add(new Player(100, 100));
 *           addUI(new Label("SCORE 0", 4, 4));
 *       }
 *
 *       @Override
 *       public void update() {
 *           // rules that involve several objects
 *       }
 *   }
 *
 *   // in main:
 *   Engine.run(new Level1(), 640, 360);
 *
 *   // later, to switch screens:
 *   Engine.setScene(new GameOverScene());
 *
 * EVERY UPDATE, IN THIS ORDER
 *   1. UI elements update (so a button knows if it was clicked)
 *   2. every object's update(), and its animation moves forward
 *   3. the scene's update()
 *   4. removed objects leave, newly added objects join (their start() is called)
 *
 * Step 4 happens after everything else, so it is always safe to add or remove
 * objects in the middle of a loop over getObjects().
 *
 * EVERY FRAME, IN THIS ORDER
 *   1. drawBackground(canvas)   clears to the background color; override it for skies and parallax
 *   2. the layers, then the objects sorted by depth, all through the camera
 *   3. the UI elements, then drawUI(canvas), in screen coordinates
 */
public class Scene {

    private final ArrayList<GameObject> objects = new ArrayList<>();
    private final ArrayList<GameObject> joining = new ArrayList<>();
    private final ArrayList<GameObject> drawOrder = new ArrayList<>();
    private final ArrayList<TileMap> layers = new ArrayList<>();
    private final ArrayList<UIElement> uiElements = new ArrayList<>();

    private Camera camera = null;
    private int screenWidth;
    private int screenHeight;
    private int backgroundColor = Color.BLACK;

    // ================================================================== override these

    /** Called once, when the scene begins. Build the scene here. */
    public void start() {
    }

    /** Called every update, after all objects updated. Put rules that involve several objects here. */
    public void update() {
    }

    /** Draws what is behind everything. By default, fills the screen with the background color. */
    public void drawBackground(Canvas canvas) {
        canvas.clear(backgroundColor);
    }

    /** Draws extra things on top of everything, in screen coordinates. Empty by default. */
    public void drawUI(Canvas canvas) {
    }

    // ================================================================== building the scene

    /** Adds an object. It joins at the end of the current update, and its start() is called then. */
    public void add(GameObject object) {
        object.setScene(this);
        joining.add(object);
    }

    /** Adds a tile map layer. Layers are drawn in the order they were added, behind all objects. */
    public void addLayer(TileMap layer) {
        layers.add(layer);
    }

    /** Adds a UI element (button, label, box...). They are drawn in the order they were added. */
    public void addUI(UIElement element) {
        uiElements.add(element);
    }

    /** Takes a UI element off the screen. */
    public void removeUI(UIElement element) {
        uiElements.remove(element);
    }

    // ================================================================== finding objects

    /** All objects in the scene. Read it, loop over it, but use add() and remove() to change it. */
    public ArrayList<GameObject> getObjects() {
        return objects;
    }

    /** The first object with this name, or null if there is none. */
    public GameObject find(String name) {
        for (GameObject object : objects) {
            if (!object.isRemoved() && object.getName().equals(name)) {
                return object;
            }
        }
        for (GameObject object : joining) {
            if (!object.isRemoved() && object.getName().equals(name)) {
                return object;
            }
        }
        return null;
    }

    /** Every object with this name (an empty list if there are none). */
    public ArrayList<GameObject> findAll(String name) {
        ArrayList<GameObject> found = new ArrayList<>();
        for (GameObject object : objects) {
            if (!object.isRemoved() && object.getName().equals(name)) {
                found.add(object);
            }
        }
        return found;
    }

    // ================================================================== settings

    /** The camera. Available from start() on. */
    public Camera getCamera() {
        return camera;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    /** Width of the screen in game pixels. */
    public int getScreenWidth() {
        return screenWidth;
    }

    /** Height of the screen in game pixels. */
    public int getScreenHeight() {
        return screenHeight;
    }

    // ================================================================== run by the SceneRunner

    /** Prepares the scene before start(): the camera needs to know the screen size. */
    void setUp(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        camera = new Camera(width, height);
    }

    void runUpdate() {
        for (UIElement element : uiElements) {
            if (element.isVisible()) {
                element.update();
            }
        }

        // A plain counting loop: objects added during this loop wait in "joining",
        // so the list does not change while we walk through it.
        for (int i = 0; i < objects.size(); i++) {
            GameObject object = objects.get(i);
            if (!object.isRemoved()) {
                object.update();
                if (object.getAnimation() != null) {
                    object.getAnimation().update();
                }
            }
        }

        update();

        removeAndJoin();
    }

    /** Takes removed objects out, then lets new objects in and calls their start(). */
    void removeAndJoin() {
        // Walk backwards: removing an item shifts the ones after it, and those we have already seen.
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (objects.get(i).isRemoved()) {
                objects.remove(i);
            }
        }
        // An object's start() may add more objects, which then join in this same loop.
        for (int i = 0; i < joining.size(); i++) {
            GameObject object = joining.get(i);
            if (!object.isRemoved()) {
                objects.add(object);
                object.start();
            }
        }
        joining.clear();
    }

    void runDraw(Canvas canvas) {
        canvas.resetCamera();
        drawBackground(canvas);

        camera.applyTo(canvas);
        for (TileMap layer : layers) {
            layer.draw(canvas);
        }
        sortByDepth();
        for (GameObject object : drawOrder) {
            if (object.isVisible()) {
                object.draw(canvas);
            }
        }

        canvas.resetCamera();
        for (UIElement element : uiElements) {
            if (element.isVisible()) {
                element.draw(canvas);
            }
        }
        drawUI(canvas);
    }

    /**
     * Fills drawOrder with the objects, lowest depth first, using insertion sort:
     * take each object and slide it left past every object with a higher depth.
     * Objects with the same depth keep the order they were added in. The list is
     * almost always already sorted from the frame before, which makes this fast.
     */
    private void sortByDepth() {
        drawOrder.clear();
        for (GameObject object : objects) {
            int position = drawOrder.size();
            while (position > 0 && drawOrder.get(position - 1).getDepth() > object.getDepth()) {
                position = position - 1;
            }
            drawOrder.add(position, object);
        }
    }
}
