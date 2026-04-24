package app;

import doorscanner.model.ScanResult;
import doorscanner.service.ScannerService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScannerGuiTest extends Application {
    private final ScannerService scannerService = new ScannerService();

    @Override
    public void start(Stage stage) {
        Label instructionsLabel = new Label("Click the field, then scan a  QR code.");
        TextField scanField = new TextField();
        scanField.setPromptText("Scanner input will appear here...");

        Label validLabel = new Label("Valid: --");
        Label memberIdLabel = new Label("Member ID: --");
        Label nameLabel = new Label("Name: --");
        Label statusLabel = new Label("Status: --");
        Label notesLabel = new Label("Notes: --");
        notesLabel.setWrapText(true);

        scanField.setOnAction(event -> {
            String rawScan = scanField.getText();
            ScanResult result = scannerService.parseScan(rawScan);

            validLabel.setText("Valid: " + result.isValid());
            memberIdLabel.setText("Member ID: " + safeValue(result.getMemberId()));
            nameLabel.setText("Name: " + safeValue(result.getMemberName()));
            statusLabel.setText("Status: " + safeValue(result.getMembershipStatus()));
            notesLabel.setText("Notes: " + safeValue(result.getNotes()));

            System.out.println("Raw scan: " + rawScan);
            System.out.println("Parsed result: " + result);

            scanField.clear();
            scanField.requestFocus();
        });

        VBox root = new VBox(
                10,
                instructionsLabel,
                scanField,
                validLabel,
                memberIdLabel,
                nameLabel,
                statusLabel,
                notesLabel
        );
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 550, 260);
        stage.setTitle("Scanner Test");
        stage.setScene(scene);
        stage.show();

        scanField.requestFocus();
    }

    private String safeValue(String value) {
        return value == null ? "--" : value;
    }

    public static void main(String[] args) {
        launch(args);
    }
}