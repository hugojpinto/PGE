package pge.audio;

/**
 * Makes retro sounds with code, no files needed, in the style of old game consoles.
 *
 * Game consoles of the 1980s could not play recorded sound the way computers do today.
 * They had a sound chip that generated a few simple waves and changed their pitch and
 * volume as the game told it to. The NES (Nintendo Entertainment System, 1983 in
 * Japan) had two square-wave channels, one triangle-wave channel and one noise
 * channel, plus a channel for very short recordings. Composers wrote all of the
 * music and sound effects for those limits, and the style is still popular today
 * under the name "chiptune". This class makes the same kinds of waves.
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
 * A tone is a shape repeated many times per second. How many times per second is its
 * frequency, measured in hertz (Hz), after the physicist Heinrich Hertz, who
 * proved in the 1880s that radio waves exist. 440 Hz is the note A above middle C.
 * Higher frequency means a higher note. People can hear from about 20 Hz to about
 * 20,000 Hz. The shape of the wave gives the tone its character:
 *
 *   SQUARE    _|‾|_|‾|_   buzzy, the classic video game sound
 *   TRIANGLE  /\/\/\/\    soft and round, good for bass
 *   SINE      ~~~~~~~~    pure and gentle, like a whistle
 *   SAW       /|/|/|/|    bright and harsh, good for alarms and "hurt"
 *   NOISE     random      hiss: explosions, drums, wind
 *
 * Every tone fades out to silence by its end, so it sounds like a plucked note
 * rather than a beep that is cut off.
 *
 * A note about the sample rate: a digital sound can only hold tones up to half its
 * sample rate (this is the Nyquist-Shannon sampling theorem). At 22,050 samples per
 * second, that is 11,025 Hz, which covers everything a retro game needs.
 */
public final class Synth {

    /** Samples per second: half of a music CD's 44,100. Plenty for retro sounds, and the files stay small. */
    public static final int SAMPLE_RATE = 22050;

    public static final int SQUARE = 0;
    public static final int TRIANGLE = 1;
    public static final int SINE = 2;
    public static final int SAW = 3;
    public static final int NOISE = 4;

    /**
     * A very short fade-in at the start of every tone. A sound that jumps from silence
     * straight to full strength makes the speaker cone jump too, which we hear as a
     * click.
     */
    private static final double ATTACK_SECONDS = 0.004;

    private Synth() {
    }

    /**
     * A tone that slides from startFrequency to endFrequency (in Hz) over the given
     * number of seconds, and fades out by the end. volume goes from 0 to 1.
     * For NOISE, the frequency sets how high or low the hiss sounds.
     *
     * HOW IT WORKS
     * We keep a number called the "phase": how far we are through the current
     * repetition of the wave, from 0 (the start) to just under 1 (the end). For each
     * sample, the wave shape turns the phase into a value between -1 and 1. A square
     * wave, for example, is 1 for the first half of each repetition and -1 for the
     * second half. Then the phase moves on by frequency / SAMPLE_RATE: at 440 Hz and
     * 22,050 samples per second, that is 440 / 22050, about 0.02, so one repetition
     * takes about 50 samples. When the phase passes 1, a new repetition begins.
     *
     * Keeping a running phase like this, instead of computing it from the time, means
     * the frequency can change smoothly in the middle of a sound without the wave
     * jumping. Electronic tone generators use the same idea, which engineers call a
     * "phase accumulator".
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
                // Noise is random values. We pick a new one every time the phase wraps
                // around and hold it until the next wrap. A higher frequency wraps more
                // often, which makes a higher-pitched hiss. The NES noise channel worked
                // in a similar way, using a circuit called a shift register to make its
                // random-looking values.
                value = noiseValue;
            } else {
                throw new IllegalArgumentException("Unknown wave " + wave + ". Use Synth.SQUARE, TRIANGLE, SINE, SAW or NOISE.");
            }

            // The "envelope" is how the volume changes over the length of the sound:
            // here it rises very quickly from 0 at the start, then falls evenly to 0 at
            // the end. Synthesizers since the 1960s shape their notes with a four-part
            // envelope called ADSR (attack, decay, sustain, release); ours is the
            // simplest version, a short attack and a long fall.
            double envelope = 1 - progress;
            double secondsIn = i / (double) SAMPLE_RATE;
            if (secondsIn < ATTACK_SECONDS) {
                envelope = envelope * secondsIn / ATTACK_SECONDS;
            }
            samples[i] = (float) (value * envelope * volume);

            // Move forward through the wave. When we pass 1, a new repetition starts:
            // we subtract the whole part and keep the fraction, so nothing is lost.
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

    /**
     * Two sounds at the same time: their samples are added together. This is really
     * what happens in the air when two sounds play at once: their pressure waves add
     * up. If the sum goes past 1 or -1, it is cut off when the sound is played, which
     * sounds harsh ("clipping"), so keep the volumes of mixed sounds low enough.
     */
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
     * The frequency of a note name: a letter A to G, optionally # (sharp, one step up)
     * or b (flat, one step down), and an octave number. "A4" is 440 Hz; "C4" is middle C
     * on a piano. This way of naming notes is called "scientific pitch notation".
     *
     * Western music divides each octave into 12 steps (the white and black keys of a
     * piano). Going up one octave doubles the frequency, so A5 is 880 Hz and A3 is
     * 220 Hz. In "equal temperament", the tuning almost all music uses today, the 12
     * steps are all the same size: each one multiplies the frequency by the 12th root
     * of 2 (about 1.0595), so that 12 of them make exactly 2. That gives the formula
     * at the end: 440 * 2 ^ (steps from A4 / 12).
     *
     * Tuning A4 to exactly 440 Hz is an international agreement (the standard ISO 16),
     * so that instruments everywhere play in tune with each other.
     */
    public static double frequencyOf(String note) {
        if (note == null || note.length() < 2) {
            throw new IllegalArgumentException("Not a note: '" + note + "'. Write notes like C4, F#5 or Bb3.");
        }
        // How many steps each letter is above C in the same octave. Octave numbers start
        // at C, so C4 is followed by D4, E4... B4, and then C5.
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
