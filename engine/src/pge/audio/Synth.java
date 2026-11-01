package pge.audio;

/**
 * Makes retro sounds with code, no files needed, in the style of old game consoles.
 *
 *   Sound jump = Sound.fromSamples(Synth.tone(Synth.SQUARE, 260, 620, 0.16, 0.4));
 *   Sound coin = Sound.fromSamples(Synth.join(
 *           Synth.tone(Synth.SQUARE, 988, 988, 0.06, 0.3),
 *           Synth.tone(Synth.SQUARE, 1319, 1319, 0.2, 0.3)));
 *
 * Every method returns samples: an array of numbers between -1 and 1, SAMPLE_RATE of
 * them per second. Sound.fromSamples(...) turns them into a sound you can play.
 *
 * WAVES
 * A tone is a shape repeated many times per second; how many times is its frequency,
 * in hertz (Hz). 440 Hz is the note A. Higher frequency, higher note. The shape
 * gives the tone its color:
 *
 *   SQUARE    _|‾|_|‾|_   buzzy, the classic video game sound
 *   TRIANGLE  /\/\/\/\    soft and round, good for bass
 *   SINE      ~~~~~~~~    pure and gentle, like a whistle
 *   SAW       /|/|/|/|    bright and harsh, good for alarms and "hurt"
 *   NOISE     random      hiss: explosions, drums, wind
 *
 * Every tone fades out to silence by its end, so it sounds like a plucked note
 * rather than a beep that is cut off.
 */
public final class Synth {

    /** Samples per second. 22,050 is plenty for retro sounds and keeps files small. */
    public static final int SAMPLE_RATE = 22050;

    public static final int SQUARE = 0;
    public static final int TRIANGLE = 1;
    public static final int SINE = 2;
    public static final int SAW = 3;
    public static final int NOISE = 4;

    /** A short fade-in, so tones start without a click. */
    private static final double ATTACK_SECONDS = 0.004;

    private Synth() {
    }

    /**
     * A tone that slides from startFrequency to endFrequency (in Hz) over the given
     * number of seconds, and fades out by the end. volume goes from 0 to 1.
     * For NOISE, the frequency sets how high or low the hiss sounds.
     */
    public static float[] tone(int wave, double startFrequency, double endFrequency, double seconds, double volume) {
        int count = (int) (seconds * SAMPLE_RATE);
        float[] samples = new float[count];

        double phase = 0;           // where we are inside one repetition of the wave: 0 to 1
        double noiseValue = 0;
        for (int i = 0; i < count; i++) {
            double progress = i / (double) count;                                       // 0 at the start, 1 at the end
            double frequency = startFrequency + (endFrequency - startFrequency) * progress;

            double value;
            if (wave == SQUARE) {
                if (phase < 0.5) {
                    value = 1;
                } else {
                    value = -1;
                }
            } else if (wave == TRIANGLE) {
                value = 1 - 4 * Math.abs(phase - 0.5);
            } else if (wave == SINE) {
                value = Math.sin(2 * Math.PI * phase);
            } else if (wave == SAW) {
                value = 2 * phase - 1;
            } else if (wave == NOISE) {
                // A new random value every time the phase wraps around: faster
                // wrapping (higher frequency) gives a higher-pitched hiss.
                value = noiseValue;
            } else {
                throw new IllegalArgumentException("Unknown wave " + wave + ". Use Synth.SQUARE, TRIANGLE, SINE, SAW or NOISE.");
            }

            // Fade in very quickly, then fade out over the whole sound.
            double envelope = 1 - progress;
            double secondsIn = i / (double) SAMPLE_RATE;
            if (secondsIn < ATTACK_SECONDS) {
                envelope = envelope * secondsIn / ATTACK_SECONDS;
            }
            samples[i] = (float) (value * envelope * volume);

            // Move forward through the wave. Each sample advances by frequency / SAMPLE_RATE
            // of a full repetition; when we pass 1, a new repetition starts.
            phase = phase + frequency / SAMPLE_RATE;
            if (phase >= 1) {
                phase = phase - Math.floor(phase);
                noiseValue = Math.random() * 2 - 1;
            }
        }
        return samples;
    }

    /** A tone at one fixed note, like "C5" or "F#4". See frequencyOf. */
    public static float[] note(int wave, String note, double seconds, double volume) {
        double frequency = frequencyOf(note);
        return tone(wave, frequency, frequency, seconds, volume);
    }

    /** Silence: useful as a pause between parts. */
    public static float[] silence(double seconds) {
        return new float[(int) (seconds * SAMPLE_RATE)];
    }

    /** One sound after the other. */
    public static float[] join(float[] first, float[] second) {
        float[] result = new float[first.length + second.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i];
        }
        for (int i = 0; i < second.length; i++) {
            result[first.length + i] = second[i];
        }
        return result;
    }

    /** Two sounds at the same time: their samples are added together. */
    public static float[] mix(float[] first, float[] second) {
        float[] result = new float[Math.max(first.length, second.length)];
        addInto(result, first, 0);
        addInto(result, second, 0);
        return result;
    }

    /**
     * Adds a sound into a longer one, starting at the given second. This is how you
     * build music: make an empty track with silence(...), then add notes into it.
     * Anything that would go past the end of the track is left out.
     */
    public static void addInto(float[] track, float[] sound, double atSeconds) {
        int start = (int) Math.round(atSeconds * SAMPLE_RATE);
        for (int i = 0; i < sound.length; i++) {
            int position = start + i;
            if (position >= 0 && position < track.length) {
                track[position] = track[position] + sound[i];
            }
        }
    }

    /**
     * The frequency of a note name: a letter A to G, optionally # (sharp) or b (flat),
     * and an octave number. "A4" is 440 Hz; "C4" is middle C on a piano.
     *
     * Each step up the 12-note scale multiplies the frequency by the 12th root of 2,
     * so 12 steps (one octave) exactly doubles it: A5 is 880 Hz.
     */
    public static double frequencyOf(String note) {
        if (note == null || note.length() < 2) {
            throw new IllegalArgumentException("Not a note: '" + note + "'. Write notes like C4, F#5 or Bb3.");
        }
        int[] stepsFromC = { 9, 11, 0, 2, 4, 5, 7 };    // A, B, C, D, E, F, G
        char letter = Character.toUpperCase(note.charAt(0));
        if (letter < 'A' || letter > 'G') {
            throw new IllegalArgumentException("Not a note: '" + note + "'. Notes start with a letter from A to G.");
        }
        int step = stepsFromC[letter - 'A'];
        int index = 1;
        if (note.charAt(index) == '#') {
            step = step + 1;
            index = index + 1;
        } else if (note.charAt(index) == 'b') {
            step = step - 1;
            index = index + 1;
        }
        int octave;
        try {
            octave = Integer.parseInt(note.substring(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a note: '" + note + "'. It should end with an octave number, like C4.");
        }
        // Count steps from A4 (440 Hz): A4 is step 9 of octave 4.
        int stepsFromA4 = (octave - 4) * 12 + (step - 9);
        return 440.0 * Math.pow(2, stepsFromA4 / 12.0);
    }
}
