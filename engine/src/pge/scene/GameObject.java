package pge.scene;

import pge.gfx.Animation;
import pge.gfx.Bitmap;
import pge.gfx.Canvas;

/**
 * Anything that lives in a Scene: the player, an enemy, a coin, a bullet...
 *
 * Make your own kind of object by extending this class, and add it to a scene:
 *
 *   public class Slime extends GameObject {
 *       public Slime(double x, double y, Bitmap picture) {
 *           super(x, y);
 *           setSprite(picture);
 *       }
 *
 *       @Override
 *       public void update() {
 *           move(0.5, 0);      // walk right, half a pixel per update
 *       }
 *   }
 *
 *   // in your scene's start():
 *   add(new Slime(100, 200, sheet.get(4)));
 *
 * From then on the scene calls update() 60 times per second and draws the object
 * for you. You never call update() or draw() yourself.
 *
 * WHAT YOU CAN OVERRIDE
 *   start()       once, when the object joins the scene
 *   update()      every update
 *   draw(canvas)  every frame. By default it draws the sprite (or animation) at (x, y).
 *
 * PICTURES
 * Give an object either a still picture with setSprite(...), or an Animation with
 * setAnimation(...). An animation wins if both are set, and the scene moves it
 * forward automatically, so you don't need to call its update().
 *
 * HITBOX
 * overlaps(other) checks whether two objects touch. It uses each object's hitbox:
 * by default the size of its picture, or a smaller box you choose with setHitbox(...).
 *
 * NAMES
 * Give important objects a name ("hero", "slime"...) to find them from other objects:
 *   Hero hero = (Hero) getScene().find("hero");
 */
public class GameObject {

    private double x;
    private double y;

    private Bitmap sprite = null;
    private Animation animation = null;
    private boolean flipX = false;
    private boolean flipY = false;
    private boolean visible = true;
    private int depth = 0;
    private String name = "";

    private boolean customHitbox = false;
    private int hitboxOffsetX = 0;
    private int hitboxOffsetY = 0;
    private int hitboxWidth = 0;
    private int hitboxHeight = 0;

    private boolean removed = false;
    private Scene scene = null;

    /** An object at (0, 0). */
    public GameObject() {
        this(0, 0);
    }

    /** An object with its top-left corner at (x, y) in the world. */
    public GameObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ================================================================== override these

    /** Called once, when the object joins the scene (just before its first update). */
    public void start() {
    }

    /** Called every update, 60 times per second. */
    public void update() {
    }

    /** Called every frame. The default draws the current picture at (x, y). */
    public void draw(Canvas canvas) {
        Bitmap picture = getPicture();
        if (picture != null) {
            canvas.drawBitmap(picture, (int) Math.floor(x), (int) Math.floor(y), flipX, flipY);
        }
    }

    // ================================================================== position

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Moves the object by this much: move(2, 0) is two pixels to the right. */
    public void move(double dx, double dy) {
        x = x + dx;
        y = y + dy;
    }

    /** The middle of the object's picture, left to right. */
    public double getCenterX() {
        return x + getWidth() / 2.0;
    }

    /** The middle of the object's picture, top to bottom. */
    public double getCenterY() {
        return y + getHeight() / 2.0;
    }

    // ================================================================== pictures

    public Bitmap getSprite() {
        return sprite;
    }

    /** A still picture for this object. */
    public void setSprite(Bitmap sprite) {
        this.sprite = sprite;
    }

    public Animation getAnimation() {
        return animation;
    }

    /**
     * An animation for this object, or null for none (then the sprite is used).
     * Setting the animation it already has does nothing, so it is safe to call every update.
     */
    public void setAnimation(Animation animation) {
        if (this.animation == animation) {
            return;
        }
        this.animation = animation;
        if (animation != null) {
            animation.restart();
        }
    }

    /** What is drawn right now: the animation's current frame, or else the sprite. */
    public Bitmap getPicture() {
        if (animation != null) {
            return animation.getFrame();
        }
        return sprite;
    }

    /** Width of the current picture (0 if there is none). */
    public int getWidth() {
        Bitmap picture = getPicture();
        if (picture == null) {
            return 0;
        }
        return picture.getWidth();
    }

    /** Height of the current picture (0 if there is none). */
    public int getHeight() {
        Bitmap picture = getPicture();
        if (picture == null) {
            return 0;
        }
        return picture.getHeight();
    }

    public boolean isFlipX() {
        return flipX;
    }

    /** Draw the picture mirrored left-right (true), for example when walking left. */
    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public boolean isVisible() {
        return visible;
    }

    /** Invisible objects are not drawn, but they still update and can still touch things. */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getDepth() {
        return depth;
    }

    /** Objects with a higher depth are drawn on top of objects with a lower one. Default 0. */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    // ================================================================== hitbox

    /**
     * Sets the part of the picture that counts for overlaps(), measured from the
     * object's top-left corner. Example: a 16x16 character whose body is 10 wide and
     * 13 tall, starting 3 pixels in from the left and top: setHitbox(3, 3, 10, 13).
     */
    public void setHitbox(int offsetX, int offsetY, int width, int height) {
        customHitbox = true;
        hitboxOffsetX = offsetX;
        hitboxOffsetY = offsetY;
        hitboxWidth = width;
        hitboxHeight = height;
    }

    /** How far the hitbox starts from the object's left edge. */
    public int getHitboxOffsetX() {
        if (customHitbox) {
            return hitboxOffsetX;
        }
        return 0;
    }

    /** How far the hitbox starts from the object's top edge. */
    public int getHitboxOffsetY() {
        if (customHitbox) {
            return hitboxOffsetY;
        }
        return 0;
    }

    public int getHitboxWidth() {
        if (customHitbox) {
            return hitboxWidth;
        }
        return getWidth();
    }

    public int getHitboxHeight() {
        if (customHitbox) {
            return hitboxHeight;
        }
        return getHeight();
    }

    /** Left edge of the hitbox, in world pixels. */
    public int getHitboxX() {
        return (int) Math.floor(x) + getHitboxOffsetX();
    }

    /** Top edge of the hitbox, in world pixels. */
    public int getHitboxY() {
        return (int) Math.floor(y) + getHitboxOffsetY();
    }

    /**
     * True if this object's hitbox and the other's overlap.
     *
     * Two rectangles overlap when each one starts before the other one ends,
     * both left-to-right and top-to-bottom.
     */
    public boolean overlaps(GameObject other) {
        if (other == null || other == this) {
            return false;
        }
        return getHitboxX() < other.getHitboxX() + other.getHitboxWidth()
                && other.getHitboxX() < getHitboxX() + getHitboxWidth()
                && getHitboxY() < other.getHitboxY() + other.getHitboxHeight()
                && other.getHitboxY() < getHitboxY() + getHitboxHeight();
    }

    // ================================================================== scene

    public String getName() {
        return name;
    }

    /** A name to find this object with scene.find(name). Several objects may share a name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The scene this object belongs to, or null before it is added to one. */
    public Scene getScene() {
        return scene;
    }

    /** Takes the object out of the scene at the end of this update. */
    public void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    /** Called by Scene.add. */
    void setScene(Scene scene) {
        this.scene = scene;
    }
}
