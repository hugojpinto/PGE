package pge;

import pge.gfx.Canvas;

/**
 * The simplest way to make a game with PGE: implement these three methods.
 *
 * The engine calls them for you:
 *
 *   init()    once, right after the window opens. Load images, set up variables.
 *   update()  exactly 60 times per second. Move things, check rules, read input.
 *   draw()    after each update. Paint the current state of the game onto the canvas.
 *
 * Keep the jobs separate: change the game in update(), only look at it in draw().
 */
public interface Game {

    void init();

    void update();

    void draw(Canvas canvas);
}
