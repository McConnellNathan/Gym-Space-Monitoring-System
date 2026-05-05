package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WalkwayPanel extends VBox {

    private static final String DARK_RED = "#CE3737";
    private static final String RED      = "#70191D";
    private static final String BROWN    = "#6B4A3A";

    private final Label walkwayLabel           = new Label("Walkway Obstructed: --");
    private final Label walkwayConfidenceLabel = new Label("Walkway Confidence: --");
    private final Label walkwayNotesLabel      = new Label("Walkway Notes: --");

    public WalkwayPanel() {
        super(6);
        Label walkwaySectionLabel = new Label("Walkway Monitoring");
        walkwaySectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_RED + ";");

        walkwayLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        walkwayConfidenceLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        walkwayNotesLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        walkwayNotesLabel.setWrapText(true);

        getChildren().addAll(walkwaySectionLabel, walkwayLabel, walkwayConfidenceLabel, walkwayNotesLabel);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: " + RED + "; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void update(boolean obstructed, double confidence, String notes) {
        walkwayLabel.setText("Walkway Obstructed: " + obstructed);
        walkwayConfidenceLabel.setText(String.format("Walkway Confidence: %.2f", confidence));
        walkwayNotesLabel.setText("Walkway Notes: " + notes);
        if (obstructed) {
            walkwayLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold;");
        } else {
            walkwayLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold;");
        }
    }

    public void setError(String msg) {
        walkwayNotesLabel.setText("Walkway Notes: error - " + msg);
    }
}
