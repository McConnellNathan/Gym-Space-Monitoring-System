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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import nu.pattern.OpenCV;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import aihazardanalyzer.service.MessageHandler;

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
    private ImageView cameraView;
    private volatile byte[] latestFrameBytes;
    private volatile boolean frameCaptureRunning = false;
    private volatile boolean occupancyRunning = false;
    private volatile boolean aggressionRunning = false;
    private volatile boolean fallRunning = false;
    private volatile boolean walkwayRunning = false;
    private boolean lastAlarmState = false;

    // Messaging variables
    private static final String CAMERA_LOCATION = "Demo Camera Zone";
    private static final String SOUND_LOCATION = "Demo Sound Zone";
    private static final long ALERT_COOLDOWN_MS = 10000;
    private MessageHandler messageHandler;
    private final Map<String, Long> lastSentAlertTimes = new ConcurrentHashMap<>();

    // UI components
    private Label peopleCountLabel;
    private Label sceneStatusLabel;
    private Label occupancyConfidenceLabel;
    private Label occupancyNotesLabel;

    private Label aggressionLabel;
    private Label aggressionConfidenceLabel;
    private Label aggressionNotesLabel;

    private Label fallLabel;
    private Label fallConfidenceLabel;
    private Label fallNotesLabel;

    private Label walkwayLabel;
    private Label walkwayConfidenceLabel;
    private Label walkwayNotesLabel;

    private Label buzzerArmedLabel;
    private Label buzzerLabel;
    private Label buzzerReasonLabel;
    private Label audioLevelLabel;

    private Timeline frameCaptureTimeline;
    private Timeline occupancyTimeline;
    private Timeline aggressionTimeline;
    private Timeline fallTimeline;
    private Timeline walkwayTimeline;
    private Timeline buzzerUiTimeline;

    @Override
    public void start(Stage stage) {
        OpenCV.loadLocally();

        cameraView = new ImageView();
        cameraView.setFitWidth(420);
        cameraView.setPreserveRatio(true);
        cameraView.setSmooth(true);

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

        alarmSoundPlayer = new AlarmSoundPlayer("/alarm.wav");

        Label titleLabel = new Label("Gym Vision Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        peopleCountLabel = new Label("People Count: --");
        sceneStatusLabel = new Label("Scene Status: --");
        occupancyConfidenceLabel = new Label("Occupancy Confidence: --");
        occupancyNotesLabel = new Label("Occupancy Notes: --");
        occupancyNotesLabel.setWrapText(true);

        aggressionLabel = new Label("Possible Conflict: --");
        aggressionConfidenceLabel = new Label("Aggression Confidence: --");
        aggressionNotesLabel = new Label("Aggression Notes: --");
        aggressionNotesLabel.setWrapText(true);

        fallLabel = new Label("Possible Fall: --");
        fallConfidenceLabel = new Label("Fall Confidence: --");
        fallNotesLabel = new Label("Fall Notes: --");
        fallNotesLabel.setWrapText(true);

        walkwayLabel = new Label("Walkway Obstructed: --");
        walkwayConfidenceLabel = new Label("Walkway Confidence: --");
        walkwayNotesLabel = new Label("Walkway Notes: --");
        walkwayNotesLabel.setWrapText(true);

        buzzerArmedLabel = new Label("Bulky Buzzer Armed: OFF");
        buzzerLabel = new Label("Bulky Buzzer: OFF");
        buzzerReasonLabel = new Label("Buzzer Reason: --");
        audioLevelLabel = new Label("Audio Level: --");

        try {
            messageHandler = MessageHandler.getInstance();
            System.out.println("Connected to AlertManager through MessageHandler.");
        } catch (IOException e) {
            System.err.println("Could not connect to AlertManager: " + e.getMessage());
        }

        VBox root = new VBox(
                12,
                titleLabel,
                cameraView,

                peopleCountLabel,
                sceneStatusLabel,
                occupancyConfidenceLabel,
                occupancyNotesLabel,

                aggressionLabel,
                aggressionConfidenceLabel,
                aggressionNotesLabel,

                fallLabel,
                fallConfidenceLabel,
                fallNotesLabel,

                walkwayLabel,
                walkwayConfidenceLabel,
                walkwayNotesLabel,

                buzzerArmedLabel,
                buzzerLabel,
                buzzerReasonLabel,
                audioLevelLabel
        );

        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 900, 950);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case T -> bulkyBuzzerService.toggleArmed();
                case B -> bulkyBuzzerService.triggerManualAlarm();
            }
        });

        stage.setTitle("GSMS AI Demo Dashboard");
        stage.setScene(scene);
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

                Platform.runLater(() -> cameraView.setImage(previewImage));
            } catch (Exception e) {
                Platform.runLater(() ->
                        occupancyNotesLabel.setText("Occupancy Notes: frame capture error - " + e.getMessage()));
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

                Platform.runLater(() -> {
                    peopleCountLabel.setText("People Count: " + stableResult.getPeopleCount());
                    peopleCountLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");
                    sceneStatusLabel.setText("Scene Status: " + stableResult.getSceneStatus());
                    occupancyConfidenceLabel.setText(String.format("Occupancy Confidence: %.2f", stableResult.getConfidence()));
                    occupancyNotesLabel.setText("Occupancy Notes: " + stableResult.getNotes());
                });
            } catch (Exception e) {
                Platform.runLater(() -> occupancyNotesLabel.setText("Occupancy Notes: error - " + e.getMessage()));
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

                Platform.runLater(() -> {
                    aggressionLabel.setText("Possible Conflict: " + stableResult.isPossibleConflict());
                    aggressionConfidenceLabel.setText(String.format("Aggression Confidence: %.2f", stableResult.getConfidence()));
                    aggressionNotesLabel.setText("Aggression Notes: " + stableResult.getNotes());

                    if (stableResult.isPossibleConflict()) {
                        aggressionLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        aggressionLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> aggressionNotesLabel.setText("Aggression Notes: error - " + e.getMessage()));
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

                Platform.runLater(() -> {
                    fallLabel.setText("Possible Fall: " + stableResult.isPossibleFall());
                    fallConfidenceLabel.setText(String.format("Fall Confidence: %.2f", stableResult.getConfidence()));
                    fallNotesLabel.setText("Fall Notes: " + stableResult.getNotes());

                    if (stableResult.isPossibleFall()) {
                        fallLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        fallLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> fallNotesLabel.setText("Fall Notes: error - " + e.getMessage()));
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

                Platform.runLater(() -> {
                    walkwayLabel.setText("Walkway Obstructed: " + stableResult.isWalkwayObstructed());
                    walkwayConfidenceLabel.setText(String.format("Walkway Confidence: %.2f", stableResult.getConfidence()));
                    walkwayNotesLabel.setText("Walkway Notes: " + stableResult.getNotes());

                    if (stableResult.isWalkwayObstructed()) {
                        walkwayLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        walkwayLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> walkwayNotesLabel.setText("Walkway Notes: error - " + e.getMessage()));
            } finally {
                walkwayRunning = false;
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    private void updateBuzzerUi() {
        BulkyBuzzerState buzzerState = bulkyBuzzerService.getCurrentState();

        buzzerArmedLabel.setText("Bulky Buzzer Armed: " + (buzzerState.isArmed() ? "ON" : "OFF"));
        buzzerLabel.setText("Bulky Buzzer: " + (buzzerState.isAlarmActive() ? "ON" : "OFF"));
        buzzerReasonLabel.setText("Buzzer Reason: " + buzzerState.getReason());
        audioLevelLabel.setText(String.format("Audio Level: %.3f", buzzerState.getCurrentLevel()));

        if (buzzerState.isArmed()) {
            buzzerArmedLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        } else {
            buzzerArmedLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
        }

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
                System.out.println("Audio AI skipped: manual trigger.");
            } else if (buzzerState.getTriggerSource() == BuzzerTriggerSource.AUDIO_THRESHOLD
                    && shouldSendAlert("SOUND_" + SOUND_LOCATION, ALERT_COOLDOWN_MS)) {
                sendSoundAlertAsync(
                        "SOUND_" + SOUND_LOCATION,
                        SOUND_LOCATION,
                        1.0
                );
            }
        }

        lastAlarmState = buzzerState.isAlarmActive();

        if (buzzerState.isAlarmActive()) {
            buzzerLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");
        } else {
            buzzerLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 18px;");
        }

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
            System.err.println("[DemoAiDashboard] MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-aggression").start(() -> {
            try {
                messageHandler.sendAggressionDetectionMessage(location, confidence);
                System.out.printf("[DemoAiDashboard] Sent aggression alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("[DemoAiDashboard] Failed to send aggression alert: " + e.getMessage());
            }
        });
    }

    private void sendFallAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("[DemoAiDashboard] MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-fall").start(() -> {
            try {
                messageHandler.sendFallDetectionMessage(location, confidence);
                System.out.printf("[DemoAiDashboard] Sent fall alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("[DemoAiDashboard] Failed to send fall alert: " + e.getMessage());
            }
        });
    }

    private void sendOvercrowdingAlertAsync(String cooldownKey, String location, double confidence, int estimatedPeople) {
        if (messageHandler == null) {
            System.err.println("[DemoAiDashboard] MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-overcrowding").start(() -> {
            try {
                messageHandler.sendOvercrowdingMessage(location, confidence, estimatedPeople);
                System.out.printf("[DemoAiDashboard] Sent overcrowding alert location=%s confidence=%.2f people=%d%n",
                        location, confidence, estimatedPeople);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("[DemoAiDashboard] Failed to send overcrowding alert: " + e.getMessage());
            }
        });
    }

    private void sendWalkwayAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("[DemoAiDashboard] MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-walkway").start(() -> {
            try {
                messageHandler.sendWalkwayObstructionMessage(location, confidence);
                System.out.printf("[DemoAiDashboard] Sent walkway alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("[DemoAiDashboard] Failed to send walkway alert: " + e.getMessage());
            }
        });
    }

    private void sendSoundAlertAsync(String cooldownKey, String location, double confidence) {
        if (messageHandler == null) {
            System.err.println("[DemoAiDashboard] MessageHandler is not connected.");
            lastSentAlertTimes.remove(cooldownKey);
            return;
        }

        Thread.ofVirtual().name("hazard-send-sound").start(() -> {
            try {
                messageHandler.sendSoundDisturbanceMessage(location, confidence);
                System.out.printf("[DemoAiDashboard] Sent sound alert location=%s confidence=%.2f%n",
                        location, confidence);
            } catch (IOException e) {
                lastSentAlertTimes.remove(cooldownKey);
                System.err.println("[DemoAiDashboard] Failed to send sound alert: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}