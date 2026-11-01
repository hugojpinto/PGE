package pge.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A sound effect or a piece of music.
 *
 *   Sound jump = Sound.load("assets/jump.wav");     // in init() or start(): loading is slow
 *   jump.play();                                     // in update(), whenever it should sound
 *
 *   Sound music = Sound.load("assets/music.wav");
 *   music.setVolume(0.5);
 *   music.loop();                                    // plays forever, until music.stop()
 *
 * Sounds can also be made with code instead of loaded from a file; see Synth.
 *
 * FILES
 * Java can read WAV files (and AIFF and AU) out of the box, but not MP3 or OGG.
 * Free tools like jsfxr (https://sfxr.me) make retro sound effects as WAV files, and
 * Audacity can convert other formats to WAV.
 *
 * HOW SOUND WORKS
 * A sound is a list of numbers, like a picture is. Each number says how far the
 * speaker should be pushed out or pulled in at one moment; thousands of them per
 * second make the speaker vibrate, and vibrations are what we hear. Our sounds have
 * 22,050 numbers ("samples") per second.
 *
 * The JDK plays sounds with a Clip: a sound loaded into memory that can be started,
 * stopped and rewound. A Clip can only play once at a time, so each Sound keeps a few
 * ("voices"). That way, collecting three coins quickly plays three overlapping
 * sounds instead of cutting the first one off.
 *
 * NO SOUND DEVICE?
 * If the computer has no working sound output, PGE prints one warning and the game
 * runs silently. A game should never crash because the speakers are missing.
 */
public class Sound {

    /** How many copies of the same sound can play at once. */
    private static final int VOICES = 4;

    private static double masterVolume = 1.0;
    private static boolean warnedNoAudio = false;
    private static final ArrayList<Sound> allSounds = new ArrayList<>();

    private final byte[] data;
    private final AudioFormat format;
    private final Clip[] clips;
    private int nextVoice = 0;
    private double volume = 1.0;

    // ================================================================== creating sounds

    /** Loads a WAV file. Do this once, in init() or start(), not every update. */
    public static Sound load(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            throw new RuntimeException("Could not find the sound '" + path + "'. "
                    + "Looked for it here: " + file.getAbsolutePath());
        }
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            // Turn whatever the file holds into plain 16-bit numbers, the format every
            // sound device understands.
            AudioFormat source = stream.getFormat();
            AudioFormat plain = new AudioFormat(source.getSampleRate(), 16, source.getChannels(), true, false);
            AudioInputStream converted = AudioSystem.getAudioInputStream(plain, stream);
            byte[] bytes = converted.readAllBytes();
            converted.close();
            return new Sound(bytes, plain);
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException("'" + path + "' is not a sound format Java understands. Save it as WAV.");
        } catch (IOException e) {
            throw new RuntimeException("Could not read the sound '" + path + "': " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("'" + path + "' uses a kind of WAV Java cannot convert. "
                    + "Try saving it again as 16-bit PCM WAV.", e);
        }
    }

    /**
     * Makes a sound from samples: numbers between -1 and 1, at Synth.SAMPLE_RATE per
     * second. Synth makes these for you; you can also compute your own.
     */
    public static Sound fromSamples(float[] samples) {
        // Each sample becomes a 16-bit whole number (-32767 to 32767), stored as two
        // bytes, the low byte first ("little-endian").
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            float sample = Math.max(-1f, Math.min(1f, samples[i]));
            int value = Math.round(sample * 32767);
            bytes[2 * i] = (byte) (value & 0xFF);
            bytes[2 * i + 1] = (byte) ((value >> 8) & 0xFF);
        }
        AudioFormat format = new AudioFormat(Synth.SAMPLE_RATE, 16, 1, true, false);
        return new Sound(bytes, format);
    }

    private Sound(byte[] data, AudioFormat format) {
        this.data = data;
        this.format = format;
        this.clips = openClips(data, format);
        allSounds.add(this);
    }

    /** Opens VOICES clips holding this sound, or none at all if there is no sound device. */
    private static Clip[] openClips(byte[] data, AudioFormat format) {
        Clip[] opened = new Clip[VOICES];
        try {
            for (int i = 0; i < VOICES; i++) {
                opened[i] = AudioSystem.getClip();
                opened[i].open(format, data, 0, data.length);
            }
            return opened;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            if (!warnedNoAudio) {
                System.err.println("PGE: sound is not available on this computer (" + e.getMessage()
                        + "). The game will run without sound.");
                warnedNoAudio = true;
            }
            return new Clip[0];
        }
    }

    // ================================================================== playing

    /** Plays the sound once, from the start. If it is already playing, another copy plays on top. */
    public void play() {
        if (clips.length == 0) {
            return;
        }
        // Use a voice that is free. If all are busy, restart the one whose turn it is.
        Clip clip = null;
        for (int i = 0; i < clips.length && clip == null; i++) {
            if (!clips[i].isRunning()) {
                clip = clips[i];
            }
        }
        if (clip == null) {
            clip = clips[nextVoice];
            nextVoice = (nextVoice + 1) % clips.length;
            clip.stop();
        }
        applyVolume(clip);
        clip.setFramePosition(0);
        clip.start();
    }

    /** Plays the sound over and over, until stop(). Meant for music. */
    public void loop() {
        if (clips.length == 0) {
            return;
        }
        stop();
        applyVolume(clips[0]);
        clips[0].setFramePosition(0);
        clips[0].loop(Clip.LOOP_CONTINUOUSLY);
    }

    /** Stops every copy of this sound that is playing. */
    public void stop() {
        for (Clip clip : clips) {
            clip.stop();
        }
    }

    /** True if any copy of this sound is playing right now. */
    public boolean isPlaying() {
        for (Clip clip : clips) {
            if (clip.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /** How long the sound is, in seconds. */
    public double getSeconds() {
        return data.length / (double) format.getFrameSize() / format.getFrameRate();
    }

    // ================================================================== volume

    public double getVolume() {
        return volume;
    }

    /** 1.0 is full volume (the default), 0.5 is half, 0 is silent. Changes playing copies too. */
    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        for (Clip clip : clips) {
            applyVolume(clip);
        }
    }

    public static double getMasterVolume() {
        return masterVolume;
    }

    /**
     * The volume of every sound at once: 1.0 normal, 0 silent. Handy for a mute key:
     *   Sound.setMasterVolume(0);
     */
    public static void setMasterVolume(double volume) {
        masterVolume = Math.max(0, Math.min(1, volume));
        for (Sound sound : allSounds) {
            for (Clip clip : sound.clips) {
                sound.applyVolume(clip);
            }
        }
    }

    /**
     * Sets a clip's loudness. Sound devices measure it in decibels (dB), where
     * 0 dB means "as recorded" and every -6 dB roughly halves it. So a volume
     * (a fraction from 0 to 1) becomes 20 * log10(volume) decibels.
     */
    private void applyVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        double fraction = volume * masterVolume;
        double decibels = gain.getMinimum();
        if (fraction > 0) {
            decibels = 20 * Math.log10(fraction);
        }
        decibels = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels));
        gain.setValue((float) decibels);
    }

    // ================================================================== saving

    /** Saves the sound as a WAV file, for example one you made with Synth. */
    public void save(String path) {
        try {
            long frames = data.length / format.getFrameSize();
            AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(data), format, frames);
            File file = new File(path);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
        } catch (IOException e) {
            throw new RuntimeException("Could not save the sound to '" + path + "': " + e.getMessage(), e);
        }
    }
}
