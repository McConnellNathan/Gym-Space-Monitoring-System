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
import protocol.Msg;
import utility.RemoteMessageClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class DoorScannerDemo extends Application {
    private static final String LOG_STORE_HOST = "localhost";
    private static final int LOG_STORE_PORT = 5000;

    private final ScannerService scannerService = new ScannerService();
    private final Set<String> checkedInMembers = new HashSet<>();

    private RemoteMessageClient logStoreClient;

    @Override
    public void start(Stage stage) {
        Label instructionsLabel = new Label("Click the field, then scan a QR code.");
        TextField scanField = new TextField();
        scanField.setPromptText("Scanner input will appear here...");

        Label validLabel = new Label("Valid: --");
        Label memberIdLabel = new Label("Member ID: --");
        Label nameLabel = new Label("Name: --");
        Label statusLabel = new Label("Status: --");
        Label actionLabel = new Label("Action: --");
        Label timestampLabel = new Label("Timestamp: --");
        Label notesLabel = new Label("Notes: --");
        notesLabel.setWrapText(true);

        try {
            logStoreClient = new RemoteMessageClient(LOG_STORE_HOST, LOG_STORE_PORT);
            System.out.printf("[DoorScannerDemo] Connected to LogStore at %s:%d%n",
                    LOG_STORE_HOST, LOG_STORE_PORT);
        } catch (IOException e) {
            System.err.println("[DoorScannerDemo] Could not connect to LogStore: " + e.getMessage());
        }

        scanField.setOnAction(event -> {
            String rawScan = scanField.getText();
            ScanResult result = scannerService.parseScan(rawScan);

            validLabel.setText("Valid: " + result.isValid());
            memberIdLabel.setText("Member ID: " + safeValue(result.getMemberId()));
            nameLabel.setText("Name: " + safeValue(result.getMemberName()));
            statusLabel.setText("Status: " + safeValue(result.getMembershipStatus()));

            if (!result.isValid() || result.getMemberId() == null) {
                actionLabel.setText("Action: REJECTED");
                timestampLabel.setText("Timestamp: " + nowString());
                notesLabel.setText("Notes: " + safeValue(result.getNotes()));

                System.out.println("[DoorScannerDemo] Invalid scan: " + rawScan);

                scanField.clear();
                scanField.requestFocus();
                return;
            }

            String memberId = result.getMemberId();
            String memberName = safeValue(result.getMemberName());

            String action;
            if (checkedInMembers.contains(memberId)) {
                checkedInMembers.remove(memberId);
                action = "SIGNED OUT";
            } else {
                checkedInMembers.add(memberId);
                action = "SIGNED IN";
            }

            actionLabel.setText("Action: " + action);
            timestampLabel.setText("Timestamp: " + nowString());
            notesLabel.setText("Notes: Entry recorded");

            System.out.printf("[DoorScannerDemo] %s (%s) %s%n", memberName, memberId, action);

            sendMemberEntryToLogStore(memberName, action);

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
                actionLabel,
                timestampLabel,
                notesLabel
        );
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 560, 320);
        stage.setTitle("Door Scanner Demo");
        stage.setScene(scene);
        stage.show();

        scanField.requestFocus();
    }

    private void sendMemberEntryToLogStore(String memberName, String action) {
        if (logStoreClient == null) {
            System.err.println("[DoorScannerDemo] LogStore client is not connected.");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            Msg.MemberEnterRecord record = new Msg.MemberEnterRecord(
                    memberName + " " + action,
                    now.getMonthValue(),
                    now.getDayOfMonth(),
                    now.getYear(),
                    now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            );

            Msg.MemberEnter msg = new Msg.MemberEnter(record);
            Msg response = logStoreClient.sendAndRead(msg);

            System.out.println("[DoorScannerDemo] LogStore response: " + response);
        } catch (Exception e) {
            System.err.println("[DoorScannerDemo] Failed to send member entry to LogStore: " + e.getMessage());
        }
    }

    private String safeValue(String value) {
        return value == null ? "--" : value;
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void stop() {
        if (logStoreClient != null) {
            try {
                logStoreClient.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
