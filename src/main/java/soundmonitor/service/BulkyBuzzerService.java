package soundmonitor.service;

import soundmonitor.model.BulkyBuzzerState;
import soundmonitor.model.BuzzerTriggerSource;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class BulkyBuzzerService {
    private static final float SAMPLE_RATE = 16000f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private static final double DEFAULT_THRESHOLD = 0.07; // Mic sensitivity - higher means lower sensitivity
    private static final long ALARM_HOLD_MS = 1500;
    private static final long COOLDOWN_MS = 2000;

    private static final int BYTES_PER_SAMPLE = SAMPLE_SIZE_BITS / 8;
    private static final int BYTES_PER_SECOND = (int) SAMPLE_RATE * CHANNELS * BYTES_PER_SAMPLE;
    private static final int ROLLING_BUFFER_SECONDS = 2;
    private static final int ROLLING_BUFFER_SIZE = BYTES_PER_SECOND * ROLLING_BUFFER_SECONDS;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean manualTrigger = new AtomicBoolean(false);

    private volatile boolean armed = false;

    private volatile BulkyBuzzerState currentState =
            new BulkyBuzzerState(false, false, 0.0, "Disarmed", BuzzerTriggerSource.NONE);

    private volatile long alarmUntilMs = 0L;
    private volatile long cooldownUntilMs = 0L;

    private TargetDataLine micLine;
    private Thread workerThread;

    private final byte[] rollingAudioBuffer = new byte[ROLLING_BUFFER_SIZE];
    private int rollingWritePos = 0;
    private int totalBufferedBytes = 0;
    private final Object audioBufferLock = new Object();

    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        workerThread = new Thread(this::runLoop, "bulky-buzzer-audio");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void stop() {
        running.set(false);

        if (micLine != null) {
            micLine.stop();
            micLine.close();
        }
    }

    public BulkyBuzzerState getCurrentState() {
        long now = System.currentTimeMillis();

        if (currentState.isAlarmActive() && now > alarmUntilMs) {
            currentState = new BulkyBuzzerState(
                    false,
                    armed,
                    currentState.getCurrentLevel(),
                    armed ? "Monitoring" : "Disarmed",
                    BuzzerTriggerSource.NONE
            );
        }

        return currentState;
    }

    public void triggerManualAlarm() {
        if (armed) {
            manualTrigger.set(true);
        }
    }

    public void toggleArmed() {
        armed = !armed;

        if (!armed) {
            alarmUntilMs = 0L;
            cooldownUntilMs = 0L;
            currentState = new BulkyBuzzerState(
                    false,
                    false,
                    currentState.getCurrentLevel(),
                    "Disarmed",
                    BuzzerTriggerSource.NONE
            );
        } else {
            currentState = new BulkyBuzzerState(
                    false,
                    true,
                    currentState.getCurrentLevel(),
                    "Monitoring",
                    BuzzerTriggerSource.NONE
            );
        }
    }

    public boolean isArmed() {
        return armed;
    }

    public byte[] getRecentAudioAsWav(int durationMs) throws Exception {
        AudioFormat format = getAudioFormat();

        int requestedBytes = (BYTES_PER_SECOND * durationMs) / 1000;
        byte[] rawAudio = getRecentRawAudio(requestedBytes);

        ByteArrayInputStream bais = new ByteArrayInputStream(rawAudio);
        AudioInputStream ais = new AudioInputStream(
                bais,
                format,
                rawAudio.length / format.getFrameSize()
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
        return baos.toByteArray();
    }

    private byte[] getRecentRawAudio(int requestedBytes) {
        synchronized (audioBufferLock) {
            int availableBytes = Math.min(totalBufferedBytes, requestedBytes);
            byte[] result = new byte[availableBytes];

            if (availableBytes == 0) {
                return result;
            }

            int startPos = (rollingWritePos - availableBytes + rollingAudioBuffer.length) % rollingAudioBuffer.length;

            if (startPos + availableBytes <= rollingAudioBuffer.length) {
                System.arraycopy(rollingAudioBuffer, startPos, result, 0, availableBytes);
            } else {
                int firstPart = rollingAudioBuffer.length - startPos;
                int secondPart = availableBytes - firstPart;

                System.arraycopy(rollingAudioBuffer, startPos, result, 0, firstPart);
                System.arraycopy(rollingAudioBuffer, 0, result, firstPart, secondPart);
            }

            return result;
        }
    }

    private void appendToRollingBuffer(byte[] buffer, int bytesRead) {
        synchronized (audioBufferLock) {
            int remaining = bytesRead;
            int srcPos = 0;

            while (remaining > 0) {
                int bytesToEnd = rollingAudioBuffer.length - rollingWritePos;
                int chunk = Math.min(remaining, bytesToEnd);

                System.arraycopy(buffer, srcPos, rollingAudioBuffer, rollingWritePos, chunk);

                rollingWritePos = (rollingWritePos + chunk) % rollingAudioBuffer.length;
                srcPos += chunk;
                remaining -= chunk;
            }

            totalBufferedBytes = Math.min(totalBufferedBytes + bytesRead, rollingAudioBuffer.length);
        }
    }

    private void runLoop() {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            micLine = (TargetDataLine) AudioSystem.getLine(info);
            micLine.open(format);
            micLine.start();

            byte[] buffer = new byte[1024];

            while (running.get()) {
                int bytesRead = micLine.read(buffer, 0, buffer.length);
                if (bytesRead <= 0) {
                    continue;
                }

                appendToRollingBuffer(buffer, bytesRead);

                double level = calculateNormalizedLevel(buffer, bytesRead);
                long now = System.currentTimeMillis();

                if (!armed) {
                    currentState = new BulkyBuzzerState(
                            false,
                            false,
                            level,
                            "Disarmed",
                            BuzzerTriggerSource.NONE
                    );
                    manualTrigger.set(false);
                    continue;
                }

                if (manualTrigger.getAndSet(false)) {
                    activateAlarm(level, "Manual trigger", BuzzerTriggerSource.MANUAL);
                    continue;
                }

                if (now < cooldownUntilMs) {
                    currentState = new BulkyBuzzerState(
                            now < alarmUntilMs,
                            true,
                            level,
                            now < alarmUntilMs ? "Alarm active" : "Cooling down",
                            now < alarmUntilMs ? currentState.getTriggerSource() : BuzzerTriggerSource.NONE
                    );
                    continue;
                }

                if (level >= DEFAULT_THRESHOLD) {
                    activateAlarm(level, "Noise threshold exceeded", BuzzerTriggerSource.AUDIO_THRESHOLD);
                } else {
                    currentState = new BulkyBuzzerState(
                            now < alarmUntilMs,
                            true,
                            level,
                            now < alarmUntilMs ? "Alarm active" : "Monitoring",
                            now < alarmUntilMs ? currentState.getTriggerSource() : BuzzerTriggerSource.NONE
                    );
                }
            }

        } catch (Exception e) {
            currentState = new BulkyBuzzerState(
                    false,
                    armed,
                    0.0,
                    "Mic error: " + e.getMessage(),
                    BuzzerTriggerSource.NONE
            );
        }
    }

    private void activateAlarm(double level, String reason, BuzzerTriggerSource triggerSource) {
        long now = System.currentTimeMillis();
        alarmUntilMs = now + ALARM_HOLD_MS;
        cooldownUntilMs = now + COOLDOWN_MS;

        currentState = new BulkyBuzzerState(true, armed, level, reason, triggerSource);
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );
    }

    private double calculateNormalizedLevel(byte[] buffer, int bytesRead) {
        int sampleCount = bytesRead / 2;
        if (sampleCount == 0) {
            return 0.0;
        }

        double sumSquares = 0.0;
        double peak = 0.0;

        for (int i = 0; i < bytesRead - 1; i += 2) {
            int low = buffer[i] & 0xff;
            int high = buffer[i + 1];
            short sample = (short) ((high << 8) | low);

            double normalized = sample / 32768.0;
            double absValue = Math.abs(normalized);

            sumSquares += normalized * normalized;
            if (absValue > peak) {
                peak = absValue;
            }
        }

        double rms = Math.sqrt(sumSquares / sampleCount);
        double combinedLevel = Math.max(rms, peak * 0.8);

        return Math.min(combinedLevel, 1.0);
    }
}