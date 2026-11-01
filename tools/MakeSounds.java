import pge.audio.Sound;
import pge.audio.Synth;

/**
 * Makes the sound effects and music of games/platformer-scenes with pge.audio.Synth,
 * and saves them as WAV files in its assets/sounds folder.
 *
 * It uses the engine, so build first, then run it from the PGE folder with the
 * engine on the class path:
 *
 *   java Build.java
 *   java -cp out/engine tools/MakeSounds.java
 *
 * Change a number, run it again, and listen. Or replace the WAV files with sounds
 * made in another tool: the game does not care where they come from.
 */
public class MakeSounds {

    static final String FOLDER = "games/platformer-scenes/assets/sounds/";

    public static void main(String[] args) {
        // Jump: a quick rising "bwoop"
        save("jump", Synth.tone(Synth.SQUARE, 260, 620, 0.16, 0.35));

        // Coin: two high notes, the second one longer
        save("coin", Synth.join(
                Synth.note(Synth.SQUARE, "B5", 0.06, 0.3),
                Synth.note(Synth.SQUARE, "E6", 0.22, 0.3)));

        // Stomp: a falling tone with a puff of noise on top
        save("stomp", Synth.mix(
                Synth.tone(Synth.SQUARE, 420, 90, 0.14, 0.35),
                Synth.tone(Synth.NOISE, 3000, 1000, 0.08, 0.25)));

        // Hurt: a harsh falling buzz
        save("hurt", Synth.mix(
                Synth.tone(Synth.SAW, 420, 110, 0.32, 0.4),
                Synth.tone(Synth.NOISE, 1500, 500, 0.12, 0.2)));

        // Click: a tiny tick for menu buttons
        save("click", Synth.tone(Synth.SQUARE, 1300, 1000, 0.04, 0.25));

        // Win: a rising arpeggio, C major
        save("win", arpeggio(new String[] { "C5", "E5", "G5", "C6" }, 0.11, 0.45));

        // Game over: a falling one
        save("gameover", arpeggio(new String[] { "G4", "E4", "C4", "G3" }, 0.16, 0.6));

        save("music", music());
    }

    /** Notes one after the other, each lasting step seconds; the last one rings for lastNote seconds. */
    static float[] arpeggio(String[] notes, double step, double lastNote) {
        float[] result = Synth.silence(0);
        for (int i = 0; i < notes.length; i++) {
            double length = step;
            if (i == notes.length - 1) {
                length = lastNote;
            }
            result = Synth.join(result, Synth.note(Synth.SQUARE, notes[i], length, 0.3));
        }
        return result;
    }

    // ===================================================================== MUSIC
    //
    // Eight bars, four chords repeated twice: C, A minor, F, G. Each bar has eight
    // steps (eighth notes). Three instruments play together:
    //   bass    triangle wave, jumping between the chord's root and its octave
    //   melody  square wave; "-" means rest
    //   hi-hat  a tick of noise on every second step
    //
    // The track is built by starting with silence and adding every note at its
    // moment, like writing notes on a page.

    static final double BEATS_PER_MINUTE = 140;
    static final double STEP = 60.0 / BEATS_PER_MINUTE / 2;    // one eighth note, in seconds

    static final String[] BASS_ROOTS = { "C2", "A1", "F1", "G1", "C2", "A1", "F1", "G1" };
    static final String[] BASS_OCTAVES = { "C3", "A2", "F2", "G2", "C3", "A2", "F2", "G2" };

    static final String[][] MELODY = {
        { "E5", "-", "G5", "-", "C6", "-", "G5", "-" },
        { "A5", "-", "E5", "-", "C5", "-", "E5", "-" },
        { "F5", "-", "A5", "-", "C6", "-", "A5", "-" },
        { "G5", "-", "B5", "-", "D6", "-", "B5", "-" },
        { "E5", "G5", "E5", "C5", "D5", "E5", "-", "-" },
        { "C5", "E5", "A5", "G5", "E5", "-", "C5", "-" },
        { "F5", "A5", "C6", "A5", "F5", "E5", "D5", "-" },
        { "G5", "-", "B4", "-", "D5", "-", "G5", "-" },
    };

    static float[] music() {
        int bars = MELODY.length;
        float[] track = Synth.silence(bars * 8 * STEP);

        for (int bar = 0; bar < bars; bar++) {
            for (int step = 0; step < 8; step++) {
                double time = (bar * 8 + step) * STEP;

                String bassNote = BASS_ROOTS[bar];
                if (step % 2 == 1) {
                    bassNote = BASS_OCTAVES[bar];
                }
                Synth.addInto(track, Synth.note(Synth.TRIANGLE, bassNote, STEP * 0.95, 0.35), time);

                String melodyNote = MELODY[bar][step];
                if (!melodyNote.equals("-")) {
                    Synth.addInto(track, Synth.note(Synth.SQUARE, melodyNote, STEP * 1.8, 0.12), time);
                }

                if (step % 2 == 1) {
                    Synth.addInto(track, Synth.tone(Synth.NOISE, 8000, 8000, 0.03, 0.06), time);
                }
            }
        }
        return track;
    }

    static void save(String name, float[] samples) {
        String path = FOLDER + name + ".wav";
        Sound.fromSamples(samples).save(path);
        System.out.println("Wrote " + path);
    }
}
