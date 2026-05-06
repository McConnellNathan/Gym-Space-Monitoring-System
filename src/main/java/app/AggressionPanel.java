package app;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AggressionPanel extends VBox {

    private static final String DARK_RED = "#CE3737";
    private static final String RED      = "#70191D";
    private static final String BROWN    = "#6B4A3A";

    private final Label aggressionLabel           = new Label("Possible Conflict: --");
    private final Label aggressionConfidenceLabel = new Label("Aggression Confidence: --");
    private final Label aggressionNotesLabel      = new Label("Aggression Notes: --");

    public AggressionPanel() {
        super(6);
        Label aggressionSectionLabel = new Label("Conflict Detection");
        aggressionSectionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + DARK_RED + ";");

        aggressionLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        aggressionConfidenceLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        aggressionNotesLabel.setStyle("-fx-text-fill: " + BROWN + ";");
        aggressionNotesLabel.setWrapText(true);
        aggressionNotesLabel.setMinHeight(60);

        getChildren().addAll(aggressionSectionLabel, aggressionLabel,
                aggressionConfidenceLabel, aggressionNotesLabel);
        setPadding(new Insets(10));
        setMinHeight(180);
        setPrefHeight(180);
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);
        setStyle("-fx-background-color: white; -fx-border-color: " + RED + "; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    public void update(boolean conflict, double confidence, String notes) {
        aggressionLabel.setText("Possible Conflict: " + conflict);
        aggressionConfidenceLabel.setText(String.format("Aggression Confidence: %.2f", confidence));
        aggressionNotesLabel.setText("Aggression Notes: " + notes);
        if (conflict) {
            aggressionLabel.setStyle("-fx-text-fill: " + DARK_RED + "; -fx-font-weight: bold;");
        } else {
            aggressionLabel.setStyle("-fx-text-fill: " + BROWN + "; -fx-font-weight: bold;");
        }
    }

    public void setError(String msg) {
        aggressionNotesLabel.setText("Aggression Notes: error - " + msg);
    }
}
