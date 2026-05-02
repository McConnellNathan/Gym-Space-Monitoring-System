package gui.customer;

import gui.common.DashboardGateway;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Class for customer dashboard
 * Hex codes:
 *  #5786BC (blue)
 *  #1B3955 (dark blue)
 *  #F6E4CE (tan)
 */

public class CustomerDashboardView {
    private final DashboardGateway dashboard;

    public CustomerDashboardView(DashboardGateway dashboard) {
        this.dashboard = dashboard;
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label title = new Label("Customer Dashboard");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label occupancyLabel = new Label();
        occupancyLabel.setStyle("-fx-font-size: 18px;");
        updateOccupancyLabel(occupancyLabel);

        Button refreshButton = new Button("Refresh Occupancy");
        refreshButton.setOnAction(e -> updateOccupancyLabel(occupancyLabel));

        VBox topBox = new VBox(10, title, occupancyLabel, refreshButton);
        topBox.setAlignment(Pos.CENTER_LEFT);

        ListView<String> classList = new ListView<>();
        classList.getItems().addAll(dashboard.getClassSchedule());

        Button signUpButton = new Button("Sign Up for Selected Class");
        Label statusLabel = new Label();

        signUpButton.setOnAction(e -> {
            String selectedClass = classList.getSelectionModel().getSelectedItem();

            if (selectedClass == null) {
                statusLabel.setText("Please select a class first.");
                return;
            }

            boolean success = dashboard.registerForClass(selectedClass);

            if (success) {
                statusLabel.setText("Successfully registered for: " + selectedClass);
            } else {
                statusLabel.setText("That class is full. Please choose another class.");
            }
        });

        VBox centerBox = new VBox(10,
                new Label("Available Classes"),
                classList,
                signUpButton,
                statusLabel
        );

        centerBox.setPadding(new Insets(20, 0, 0, 0));

        root.setTop(topBox);
        root.setCenter(centerBox);

        return root;
    }

    private void updateOccupancyLabel(Label label) {
        label.setText("Current Gym Occupancy: "
                + dashboard.getCurrentOccupancy()
                + " / "
                + dashboard.getMaxOccupancy());
    }
}
