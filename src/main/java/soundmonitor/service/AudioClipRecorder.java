package soundmonitor.service;

import javax.sound.sampled.*;

public class AudioClipRecorder {
    private static final float SAMPLE_RATE = 16000f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    public byte[] recordWavClip(int durationMs) throws Exception {
        AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        int bytesPerSecond = (int) (SAMPLE_RATE * CHANNELS * (SAMPLE_SIZE_BITS / 8.0));
        int totalBytes = (bytesPerSecond * durationMs) / 1000;

        byte[] rawAudio = new byte[totalBytes];
        int bytesRead = 0;

        while (bytesRead < totalBytes) {
            bytesRead += line.read(rawAudio, bytesRead, totalBytes - bytesRead);
        }

        line.stop();
        line.close();

        return wavWrap(rawAudio, format);
    }

    private byte[] wavWrap(byte[] rawAudio, AudioFormat format) throws Exception {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(rawAudio);
        AudioInputStream ais = new AudioInputStream(
                bais,
                format,
                rawAudio.length / format.getFrameSize()
        );

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
        return baos.toByteArray();
    }
}