import pge.audio.Sound;

/**
 * All the game's sounds, loaded once and shared by every scene and object:
 *
 *   Sounds.jump.play();
 *
 * The fields are "static": they belong to the class itself, not to an object, so
 * any code can reach them as Sounds.something. That suits things the whole game
 * shares, like sounds. The files are made by tools/MakeSounds.java.
 */
public class Sounds {

    public static Sound jump;
    public static Sound coin;
    public static Sound stomp;
    public static Sound hurt;
    public static Sound click;
    public static Sound win;
    public static Sound gameOver;
    public static Sound music;

    private static boolean loaded = false;
    private static boolean muted = false;

    /** Loads every sound and starts the music. Safe to call more than once: only the first time does anything. */
    public static void loadAll() {
        if (loaded) {
            return;
        }
        jump = Sound.load("assets/sounds/jump.wav");
        coin = Sound.load("assets/sounds/coin.wav");
        stomp = Sound.load("assets/sounds/stomp.wav");
        hurt = Sound.load("assets/sounds/hurt.wav");
        click = Sound.load("assets/sounds/click.wav");
        win = Sound.load("assets/sounds/win.wav");
        gameOver = Sound.load("assets/sounds/gameover.wav");
        music = Sound.load("assets/sounds/music.wav");

        coin.setVolume(0.7);
        music.setVolume(0.45);
        music.loop();
        loaded = true;
    }

    /** Turns all sound off, or back on. */
    public static void toggleMute() {
        muted = !muted;
        if (muted) {
            Sound.setMasterVolume(0);
        } else {
            Sound.setMasterVolume(1);
        }
    }
}
