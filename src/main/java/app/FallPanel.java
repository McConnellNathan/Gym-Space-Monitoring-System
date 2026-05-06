package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FallPanel extends VBox {

    private static final String DARK_RED = "#CE3737";
    private static final String RED      = "#70191D";
    private static final String BROWN    = "#6B4A3A";

    private final Label fallLabel           = new Label("Possible Fall: --");
    private final Label fallConfidenceLabel = new Label("Fall Confidence: --");
    private final Label fallNotesLabel      = new Label("Fall Notes: --");

    public FallPanel() {
        super(6);
        Label fallSectionLabel = new Label("Fall Detection");
        fallSectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_RED + ";");

        fallLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        fallConfidenceLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        fallNotesLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        fallNotesLabel.setWrapText(true);
        fallNotesLabel.setMinHeight(60);

        getChildren().addAll(fallSectionLabel, fallLabel, fallConfidenceLabel, fallNotesLabel);
        setPadding(new Insets(10));
        setMinHeight(180);
        setPrefHeight(180);
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        setStyle("-fx-background-color: white; -fx-border-color: " + RED + "; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void update(boolean fall, double confidence, String notes) {
        fallLabel.setText("Possible Fall: " + fall);
        fallConfidenceLabel.setText(String.format("Fall Confidence: %.2f", confidence));
        fallNotesLabel.setText("Fall Notes: " + notes);
        if (fall) {
            fallLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold;");
        } else {
            fallLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold;");
        }
    }

    public void setError(String msg) {
        fallNotesLabel.setText("Fall Notes: error - " + msg);
    }
}
