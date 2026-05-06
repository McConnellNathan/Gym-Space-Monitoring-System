package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import soundmonitor.model.AudioAnalysisResult;
import soundmonitor.model.BulkyBuzzerState;

public class SoundPanel extends VBox {

    private static final String DARK_RED = "#CE3737";
    private static final String RED      = "#70191D";
    private static final String BROWN    = "#6B4A3A";

    private final Label buzzerArmedLabel     = new Label("Bulky Buzzer Armed: OFF");
    private final Label buzzerLabel          = new Label("Bulky Buzzer: OFF");
    private final Label buzzerReasonLabel    = new Label("Buzzer Reason: --");
    private final Label audioLevelLabel      = new Label("Audio Level: --");
    private final Label audioTypeLabel       = new Label("Detected Sound Type: --");
    private final Label audioConfidenceLabel = new Label("Sound Analysis Confidence: --");
    private final Label audioNotesLabel      = new Label("Sound Analysis Notes: --");

    public SoundPanel() {
        super(6);
        Label soundSectionLabel = new Label("Sound Monitoring");
        soundSectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_RED + ";");

        buzzerArmedLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        buzzerLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        buzzerReasonLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        audioLevelLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        audioTypeLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        audioConfidenceLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        audioNotesLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        audioNotesLabel.setWrapText(true);
        audioNotesLabel.setMinHeight(60);

        getChildren().addAll(soundSectionLabel, buzzerArmedLabel, buzzerLabel, buzzerReasonLabel,
                audioLevelLabel, audioTypeLabel, audioConfidenceLabel, audioNotesLabel);
        setPadding(new Insets(10));
        setMinHeight(250);
        setPrefHeight(250);
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        setStyle("-fx-background-color: white; -fx-border-color: " + RED + "; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void updateBuzzerState(BulkyBuzzerState state) {
        buzzerArmedLabel.setText("Bulky Buzzer Armed: " + (state.isArmed() ? "ON" : "OFF"));
        buzzerLabel.setText("Bulky Buzzer: " + (state.isAlarmActive() ? "ON" : "OFF"));
        buzzerReasonLabel.setText("Buzzer Reason: " + state.getReason());
        audioLevelLabel.setText(String.format("Audio Level: %.3f", state.getCurrentLevel()));

        if (state.isArmed()) {
            buzzerArmedLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold;");
        } else {
            buzzerArmedLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold;");
        }

        if (state.isAlarmActive()) {
            buzzerLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold; -fx-font-size: 18px;");
        } else {
            buzzerLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold; -fx-font-size: 18px;");
        }
    }

    public void setManualTrigger() {
        audioTypeLabel.setText("Detected Sound Type: MANUAL_TRIGGER");
        audioConfidenceLabel.setText("Sound Analysis Confidence: 1.00");
        audioNotesLabel.setText("Sound Analysis Notes: Alarm activated manually.");
    }

    public void setAudioAnalyzing() {
        audioTypeLabel.setText("Detected Sound Type: analyzing...");
        audioConfidenceLabel.setText("Sound Analysis Confidence: --");
        audioNotesLabel.setText("Sound Analysis Notes: Sending recent audio clip for analysis...");
    }

    public void updateAudioResult(AudioAnalysisResult result) {
        audioTypeLabel.setText("Detected Sound Type: " + safeValue(result.getSoundType()));
        audioConfidenceLabel.setText(String.format("Sound Analysis Confidence: %.2f", result.getConfidence()));
        audioNotesLabel.setText("Sound Analysis Notes: " + safeValue(result.getNotes()));
        if (result.isTriggerAlarm()) {
            audioTypeLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold;");
        } else {
            audioTypeLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold;");
        }
    }

    public void setAudioError(String msg) {
        audioTypeLabel.setText("Detected Sound Type: error");
        audioConfidenceLabel.setText("Sound Analysis Confidence: --");
        audioNotesLabel.setText("Sound Analysis Notes: " + msg);
    }

    private String safeValue(String value) {
        return value == null ? "--" : value;
    }
}
