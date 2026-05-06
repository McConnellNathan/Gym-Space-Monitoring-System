package app;

import aihazardanalyzer.capture.WebcamFrameCapture;
import aihazardanalyzer.model.AggressionAnalysisResult;
import aihazardanalyzer.model.FallAnalysisResult;
import aihazardanalyzer.model.OccupancyAnalysisResult;
import aihazardanalyzer.model.WalkwayAnalysisResult;
import aihazardanalyzer.service.AggressionAnalysisService;
import aihazardanalyzer.service.AggressionResultStabilizer;
import aihazardanalyzer.service.FallAnalysisService;
import aihazardanalyzer.service.FallResultStabilizer;
import aihazardanalyzer.service.OccupancyAnalysisService;
import aihazardanalyzer.service.OccupancyResultStabilizer;
import aihazardanalyzer.service.WalkwayAnalysisService;
import aihazardanalyzer.service.WalkwayResultStabilizer;
import soundmonitor.model.AudioAnalysisResult;
import soundmonitor.model.BulkyBuzzerState;
import soundmonitor.model.BuzzerTriggerSource;
import soundmonitor.service.AlarmSoundPlayer;
import soundmonitor.service.BulkyBuzzerService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import nu.pattern.OpenCV;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import aihazardanalyzer.service.MessageHandler;
import soundmonitor.service.AudioAnalysisService;

public class DemoAiDashboard extends Application {

    // AI and hardware service variables
    private WebcamFrameCapture webcam;
    private OccupancyAnalysisService occupancyAnalysisService;
    private AggressionAnalysisService aggressionAnalysisService;
    private FallAnalysisService fallAnalysisService;
    private WalkwayAnalysisService walkwayAnalysisService;
    private OccupancyResultStabilizer occupancyStabilizer;
    private AggressionResultStabilizer aggressionStabilizer;
    private FallResultStabilizer fallStabilizer;
    private WalkwayResultStabilizer walkwayStabilizer;
    private BulkyBuzzerService bulkyBuzzerService;
    private AlarmSoundPlayer alarmSoundPlayer;
    private volatile OccupancyAnalysisResult latestOccupancyResult;
    private volatile AggressionAnalysisResult latestAggressionResult;
    private volatile FallAnalysisResult latestFallResult;
    private volatile WalkwayAnalysisResult latestWalkwayResult;
    private volatile AudioAnalysisResult latestAudioResult;
    private volatile byte[] latestFrameBytes;
    private volatile boolean frameCaptureRunning = false;
    private volatile boolean occupancyRunning = false;
    private volatile boolean aggressionRunning = false;
    private volatile boolean fallRunning = false;
    private volatile boolean walkwayRunning = false;
    private boolean lastAlarmState = false;
    private AudioAnalysisService audioAnalysisService;
    private volatile boolean audioAnalysisRunning = false;

    // Messaging variables
    private static final String CAMERA_LOCATION = "Demo Camera Zone";
    private static final String SOUND_LOCATION = "Demo Sound Zone";
    private static final long ALERT_COOLDOWN_MS = 10000;
    private MessageHandler messageHandler;
    private final Map<String, Long> lastSentAlertTimes = new ConcurrentHashMap<>();

    // View
    private DemoAiDashboardView view;

    private Timeline frameCaptureTimeline;
    private Timeline occupancyTimeline;
    private Timeline aggressionTimeline;
    private Timeline fallTimeline;
    private Timeline walkwayTimeline;
    private Timeline buzzerUiTimeline;

    @Override
    public void start(Stage stage) {
        OpenCV.loadLocally();

        webcam = new WebcamFrameCapture(0, 640);

        occupancyAnalysisService = new OccupancyAnalysisService();
        aggressionAnalysisService = new AggressionAnalysisService();
        fallAnalysisService = new FallAnalysisService();
        walkwayAnalysisService = new WalkwayAnalysisService();

        occupancyStabilizer = new OccupancyResultStabilizer(3);
        aggressionStabilizer = new AggressionResultStabilizer(3);
        fallStabilizer = new FallResultStabilizer(3);
        walkwayStabilizer = new WalkwayResultStabilizer(3);

        bulkyBuzzerService = new BulkyBuzzerService();
        bulkyBuzzerService.start();

        audioAnalysisService = new AudioAnalysisService();

        alarmSoundPlayer = new AlarmSoundPlayer("/alarm.wav");

        try {
            messageHandler = MessageHandler.getInstance();
            System.out.println("Connected to AlertManager through MessageHandler.");
        } catch (IOException e) {
            System.err.println("Could not connect to AlertManager: " + e.getMessage());
        }

        view = new DemoAiDashboardView();
        Scene scene = view.getScene();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case T -> bulkyBuzzerService.toggleArmed();
                case B -> bulkyBuzzerService.triggerManualAlarm();
            }
        });

        stage.setTitle("GSMS AI Demo Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(1025);
        stage.centerOnScreen();
        stage.show();

        startFrameCaptureLoop();
        startOccupancyLoop();
        startAggressionLoop();
        startFallLoop();
        startWalkwayLoop();
        startBuzzerUiLoop();

        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        });
    }

    private void startFrameCaptureLoop() {
        frameCaptureTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), event -> updateLatestFrame())
        );
        frameCaptureTimeline.setCycleCount(Timeline.INDEFINITE);
        frameCaptureTimeline.play();
    }

    private void startOccupancyLoop() {
        occupancyTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateOccupancy())
        );
        occupancyTimeline.setCycleCount(Timeline.INDEFINITE);
        occupancyTimeline.play();
    }

    private void startAggressionLoop() {
        aggressionTimeline = new Timeline(
                new KeyFrame(Duration.millis(1500), event -> updateAggression())
        );
        aggressionTimeline.setCycleCount(Timeline.INDEFINITE);
        aggressionTimeline.play();
    }

    private void startFallLoop() {
        fallTimeline = new Timeline(
                new KeyFrame(Duration.millis(1500), event -> updateFall())
        );
        fallTimeline.setCycleCount(Timeline.INDEFINITE);
        fallTimeline.play();
    }

    private void startWalkwayLoop() {
        walkwayTimeline = new Timeline(
                new KeyFrame(Duration.millis(2500), event -> updateWalkway())
        );
        walkwayTimeline.setCycleCount(Timeline.INDEFINITE);
        walkwayTimeline.play();
    }

    private void startBuzzerUiLoop() {
        buzzerUiTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), event -> updateBuzzerUi())
        );
        buzzerUiTimeline.setCycleCount(Timeline.INDEFINITE);
        buzzerUiTimeline.play();
    }

    private void updateLatestFrame() {
        if (frameCaptureRunning) {
            return;
        }

        frameCaptureRunning = true;

        Thread worker = new Thread(() -> {
            try {
                byte[] frameBytes = webcam.captureJpegFrame();
                latestFrameBytes = Arrays.copyOf(frameBytes, frameBytes.length);

                Image previewImage = new Image(new ByteArrayInputStream(frameBytes));

                Platform.runLater(() -> view.getCameraView().setImage(previewImage));
            } catch (Exception e) {
                Platform.runLater(() -> view.getOccupancyPanel().setFrameError(e.getMessage()));
            } finally {
                frameCaptureRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateOccupancy() {
        if (occupancyRunning || latestFrameBytes == null) {
            return;
        }

        occupancyRunning = true;
        byte[] frameCopy = Arrays.copyOf(latestFrameBytes, latestFrameBytes.length);

        Thread worker = new Thread(() -> {
            try {
                OccupancyAnalysisResult rawResult = occupancyAnalysisService.analyze(frameCopy, "image/jpeg");
                OccupancyAnalysisResult stableResult = occupancyStabilizer.addAndStabilize(rawResult);
                latestOccupancyResult = stableResult;

                if ("CROWDING".equalsIgnoreCase(stableResult.getSceneStatus())
                        && shouldSendAlert("OVERCROWDING_" + CAMERA_LOCATION, 15000)) {
                    sendOvercrowdingAlertAsync(
                            "OVERCROWDING_" + CAMERA_LOCATION,
                            CAMERA_LOCATION,
                            stableResult.getConfidence(),
                            stableResult.getPeopleCount()
                    );
                }

                Platform.runLater(() -> view.getOccupancyPanel().update(
                        stableResult.getPeopleCount(),
                        stableResult.getSceneStatus(),
                        stableResult.getConfidence(),
                        stableResult.getNotes()
                ));
            } catch (Exception e) {
                Platform.runLater(() -> view.getOccupancyPanel().setError(e.getMessage()));
            } finally {
                occupancyRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateAggression() {
        if (aggressionRunning || latestFrameBytes == null) {
            return;
        }

        aggressionRunning = true;
        byte[] frameCopy = Arrays.copyOf(latestFrameBytes, latestFrameBytes.length);

        Thread worker = new Thread(() -> {
            try {
                AggressionAnalysisResult rawResult = aggressionAnalysisService.analyze(frameCopy, "image/jpeg");
                AggressionAnalysisResult stableResult = aggressionStabilizer.addAndStabilize(rawResult);
                latestAggressionResult = stableResult;

                if (stableResult.isPossibleConflict()
                        && shouldSendAlert("AGGRESSION_" + CAMERA_LOCATION, ALERT_COOLDOWN_MS)) {
                    sendAggressionAlertAsync(
                            "AGGRESSION_" + CAMERA_LOCATION,
                            CAMERA_LOCATION,
                            stableResult.getConfidence()
                    );
                }

                Platform.runLater(() -> view.getAggressionPanel().update(
                        stableResult.isPossibleConflict(),
                        stableResult.getConfidence(),
                        stableResult.getNotes()
                ));
            } catch (Exception e) {
                Platform.runLater(() -> view.getAggressionPanel().setError(e.getMessage()));
            } finally {
                aggressionRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateFall() {
        if (fallRunning || latestFrameBytes == null) {
            return;
        }

        fallRunning = true;
        byte[] frameCopy = Arrays.copyOf(latestFrameBytes, latestFrameBytes.length);

        Thread worker = new Thread(() -> {
            try {
                FallAnalysisResult rawResult = fallAnalysisService.analyze(frameCopy, "image/jpeg");
                FallAnalysisResult stableResult = fallStabilizer.addAndStabilize(rawResult);
                latestFallResult = stableResult;

                if (stableResult.isPossibleFall()
                        && shouldSendAlert("FALL_" + CAMERA_LOCATION, ALERT_COOLDOWN_MS)) {
                    sendFallAlertAsync(
                            "FALL_" + CAMERA_LOCATION,
                            CAMERA_LOCATION,
                            stableResult.getConfidence()
                    );
                }

                Platform.runLater(() -> view.getFallPanel().update(
                        stableResult.isPossibleFall(),
                        stableResult.getConfidence(),
                        stableResult.getNotes()
                ));
            } catch (Exception e) {
                Platform.runLater(() -> view.getFallPanel().setError(e.getMessage()));
            } finally {
                fallRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateWalkway() {
        if (walkwayRunning || latestFrameBytes == null) {
            return;
        }

        walkwayRunning = true;
        byte[] frameCopy = Arrays.copyOf(latestFrameBytes, latestFrameBytes.length);

        Thread worker = new Thread(() -> {
            try {
                WalkwayAnalysisResult rawResult = walkwayAnalysisService.analyze(frameCopy, "image/jpeg");
                WalkwayAnalysisResult stableResult = walkwayStabilizer.addAndStabilize(rawResult);
                latestWalkwayResult = stableResult;

                if (stableResult.isWalkwayObstructed()
                        && shouldSendAlert("WALKWAY_" + CAMERA_LOCATION, ALERT_COOLDOWN_MS)) {
                    sendWalkwayAlertAsync(
                            "WALKWAY_" + CAMERA_LOCATION,
                            CAMERA_LOCATION,
                            stableResult.getConfidence()
                    );
                }

                Platform.runLater(() -> view.getWalkwayPanel().update(
                        stableResult.isWalkwayObstructed(),
                        stableResult.getConfidence(),
                        stableResult.getNotes()
                ));
            } catch (Exception e) {
                Platform.runLater(() -> view.getWalkwayPanel().setError(e.getMessage()));
            } finally {
                walkwayRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateBuzzerUi() {
        BulkyBuzzerState buzzerState = bulkyBuzzerService.getCurrentState();

        view.getSoundPanel().updateBuzzerState(buzzerState);

        if (buzzerState.isAlarmActive() && !lastAlarmState) {
            alarmSoundPlayer.playAsync();

            if (buzzerState.getTriggerSource() == BuzzerTriggerSource.MANUAL) {
                latestAudioResult = new AudioAnalysisResult(
                        false,
                        true,
                        "MANUAL_TRIGGER",
                        1.0,
                        "Alarm activated manually."
                );

                view.getSoundPanel().setManualTrigger();

                System.out.println("Audio AI skipped: manual trigger.");
            } else if (buzzerState.getTriggerSource() == BuzzerTriggerSource.AUDIO_THRESHOLD) {
                view.getSoundPanel().setAudioAnalyzing();

                analyzeRecentAudioClip();

                if (shouldSendAlert("SOUND_" + SOUND_LOCATION, ALERT_COOLDOWN_MS)) {
                    sendSoundAlertAsync(
                            "SOUND_" + SOUND_LOCATION,
                            SOUND_LOCATION,
                            1.0
                    );
                }
            }
        }

        boolean currentAlarmState = buzzerState.isAlarmActive();
        if (currentAlarmState != lastAlarmState) {
            view.setAlarmBlinking(currentAlarmState);
        }
        lastAlarmState = currentAlarmState;
    }

    private void stopAllTimelines() {
        if (frameCaptureTimeline != null) frameCaptureTimeline.stop();
        if (occupancyTimeline != null) occupancyTimeline.stop();
        if (aggressionTimeline != null) aggressionTimeline.stop();
        if (fallTimeline != null) fallTimeline.stop();
        if (walkwayTimeline != null) walkwayTimeline.stop();
        if (buzzerUiTimeline != null) buzzerUiTimeline.stop();
    }

    @Override
    public void stop() {
        stopAllTimelines();

        if (webcam != null) {
            webcam.release();
        }
        if (bulkyBuzzerService != null) {
            bulkyBuzzerService.stop();
        }
        if (alarmSoundPlayer != null) {
            alarmSoundPlayer.close();
        }
        if (messageHandler != null) {
            try {
                messageHandler.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean shouldSendAlert(String key, long cooldownMs) {
        long now = System.currentTimeMillis();
        Long lastSent = lastSentAlertTimes.get(key);

        if (lastSent != null && now - lastSent < cooldownMs) {
            return false;
        }

        lastSentAlertTimes.put(key, now);
        return true;
    }

    private void sendAggressionAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-aggression").start(() -> {
            try {
                messageHandler.sendAggressionDetectionMessage(location, confidence);
                System.out.printf("Sent aggression alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send aggression alert: " + e.getMessage());
            }
        });
    }

    private void sendFallAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-fall").start(() -> {
            try {
                messageHandler.sendFallDetectionMessage(location, confidence);
                System.out.printf("Sent fall alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send fall alert: " + e.getMessage());
            }
        });
    }

    private void sendOvercrowdingAlertAsync(String cooldownKey, String location, double confidence, int estimatedPeople) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-overcrowding").start(() -> {
            try {
                messageHandler.sendOvercrowdingMessage(location, confidence, estimatedPeople);
                System.out.printf("Sent overcrowding alert location=%s confidence=%.2f people=%d%n",
                        location, confidence, estimatedPeople);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send overcrowding alert: " + e.getMessage());
            }
        });
    }

    private void sendWalkwayAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-walkway").start(() -> {
            try {
                messageHandler.sendWalkwayObstructionMessage(location, confidence);
                System.out.printf("Sent walkway alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send walkway alert: " + e.getMessage());
            }
        });
    }

    private void sendSoundAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-sound").start(() -> {
            try {
                messageHandler.sendSoundDisturbanceMessage(location, confidence);
                System.out.printf("Sent sound alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("Failed to send sound alert: " + e.getMessage());
            }
        });
    }

    private void analyzeRecentAudioClip() {
        if (audioAnalysisRunning) {
            return;
        }

        audioAnalysisRunning = true;

        Thread worker = new Thread(() -> {
            try {
                byte[] wavBytes = bulkyBuzzerService.getRecentAudioAsWav(1500);
                AudioAnalysisResult result = audioAnalysisService.analyzeWavClip(wavBytes);
                latestAudioResult = result;

                Platform.runLater(() -> view.getSoundPanel().updateAudioResult(result));
            } catch (Exception e) {
                Platform.runLater(() -> view.getSoundPanel().setAudioError(e.getMessage()));
            } finally {
                audioAnalysisRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
