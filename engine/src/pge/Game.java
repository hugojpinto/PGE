package pge;

import pge.gfx.Canvas;

/**
 * The simplest way to make a game with PGE: write a class that implements these
 * three methods, and pass it to Engine.run(...).
 *
 * The engine calls the methods for you:
 *
 *   init()    once, right after the window opens. Load pictures and sounds here.
 *   update()  exactly 60 times per second. Move things, check the rules, read the keys.
 *   draw()    after each update. Draw the game as it is right now onto the canvas.
 *
 * Keep the two jobs apart: update() changes the game, draw() only looks at it. Then
 * the picture always matches the state of the game, and bugs are easier to find.
 *
 * An interface lists methods without saying what they do; each class that
 * "implements" it fills them in. This is how the engine can run your game without
 * knowing anything about it: it only needs to know that your class has these three
 * methods. The same three-part split is used by many game tools. The fantasy console
 * Pico-8, for example, calls functions named _init, _update and _draw.
 */
public interface Game {

    /** Runs once, before the first update. */
    void init();

    /** Runs 60 times per second. Changes the state of the game. */
    void update();

    /** Runs after each update. Draws the state of the game, without changing it. */
    void draw(Canvas canvas);
}
