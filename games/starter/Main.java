import pge.Engine;
import pge.Game;
import pge.audio.Sound;
import pge.audio.Synth;
import pge.gfx.Canvas;
import pge.gfx.Color;
import pge.input.Input;
import pge.input.Key;

/**
 * A starting point for a new game. Copy this folder, rename the copy, and run it:
 *
 *   java Build.java mygame
 *
 * Right now it is a square you can move with the arrow keys and a score that goes
 * up (with a blip) when you press Space. Change anything you like, run it again, and
 * see what happens. That is the quickest way to learn how it works.
 */
public class Main implements Game {

    // The "state" of the game: everything that can change while playing. update()
    // changes these fields and draw() shows them.
    double playerX = 312;
    double playerY = 172;
    double playerSpeed = 2;
    int score = 0;

    Sound blip;

    public static void main(String[] args) {
        Engine.run(new Main(), 640, 360, "My Game");
    }

    /** Runs once at the start. Load pictures and set things up here. */
    @Override
    public void init() {
        // A short rising tone, made with code: square wave, from 660 Hz to 990 Hz, in 0.08 seconds.
        blip = Sound.fromSamples(Synth.tone(Synth.SQUARE, 660, 990, 0.08, 0.3));
    }

    /** Runs 60 times per second. Change the state of the game here, but don't draw. */
    @Override
    public void update() {
        if (Input.isDown(Key.LEFT)) {
            playerX = playerX - playerSpeed;
        }
        if (Input.isDown(Key.RIGHT)) {
            playerX = playerX + playerSpeed;
        }
        if (Input.isDown(Key.UP)) {
            playerY = playerY - playerSpeed;
        }
        if (Input.isDown(Key.DOWN)) {
            playerY = playerY + playerSpeed;
        }
        if (Input.wasPressed(Key.SPACE)) {
            score = score + 1;
            blip.play();
        }
        if (Input.wasPressed(Key.ESCAPE)) {
            Engine.stop();
        }
    }

    /** Runs after every update. Draw the state of the game here, but don't change it. */
    @Override
    public void draw(Canvas canvas) {
        canvas.clear(Color.DARK_BLUE);
        canvas.fillRect((int) playerX, (int) playerY, 16, 16, Color.YELLOW);
        canvas.drawText("Score: " + score, 4, 4, Color.WHITE);
        canvas.drawText("Arrows: move   Space: score   Esc: quit", 4, canvas.getHeight() - 12, Color.GRAY);
    }
}
