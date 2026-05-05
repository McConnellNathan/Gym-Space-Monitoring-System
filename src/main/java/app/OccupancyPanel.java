package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class OccupancyPanel extends VBox {

    private static final String DARK_RED = "#CE3737";
    private static final String RED      = "#70191D";
    private static final String BROWN    = "#6B4A3A";

    private final Label peopleCountLabel         = new Label("People Count: --");
    private final Label sceneStatusLabel         = new Label("Scene Status: --");
    private final Label occupancyConfidenceLabel = new Label("Occupancy Confidence: --");
    private final Label occupancyNotesLabel      = new Label("Occupancy Notes: --");

    public OccupancyPanel() {
        super(6);
        Label occupancySectionLabel = new Label("Occupancy Analysis");
        occupancySectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_RED + ";");

        sceneStatusLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        occupancyConfidenceLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        occupancyNotesLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        occupancyNotesLabel.setWrapText(true);

        getChildren().addAll(occupancySectionLabel, peopleCountLabel, sceneStatusLabel,
                occupancyConfidenceLabel, occupancyNotesLabel);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: " + RED + "; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void update(int count, String status, double confidence, String notes) {
        peopleCountLabel.setText("People Count: " + count);
        peopleCountLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        sceneStatusLabel.setText("Scene Status: " + status);
        occupancyConfidenceLabel.setText(String.format("Occupancy Confidence: %.2f", confidence));
        occupancyNotesLabel.setText("Occupancy Notes: " + notes);
    }

    public void setFrameError(String msg) {
        occupancyNotesLabel.setText("Occupancy Notes: frame capture error - " + msg);
    }

    public void setError(String msg) {
        occupancyNotesLabel.setText("Occupancy Notes: error - " + msg);
    }
}
