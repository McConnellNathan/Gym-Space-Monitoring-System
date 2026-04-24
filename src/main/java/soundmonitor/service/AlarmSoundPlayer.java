package soundmonitor.service;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class AlarmSoundPlayer {
    private Clip clip;

    public AlarmSoundPlayer(String resourcePath) {
        try {
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl == null) {
                throw new IllegalArgumentException("Sound resource not found: " + resourcePath);
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load alarm sound: " + e.getMessage(), e);
        }
    }

    public void play() {
        if (clip == null) {
            return;
        }

        if (clip.isRunning()) {
            clip.stop();
        }

        clip.setFramePosition(0);
        clip.start();
    }

    public void playAsync() {
        Thread soundThread = new Thread(this::play);
        soundThread.setDaemon(true);
        soundThread.start();
    }

    public void close() {
        if (clip != null) {
            clip.close();
        }
    }
}
