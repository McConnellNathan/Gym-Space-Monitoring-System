package gui;

import gui.common.MockDashboardData;
import gui.customer.CustomerDashboardView;
import gui.employee.EmployeeDashboardView;
import gui.manager.ManagerDashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainGuiApp extends Application {

    private final MockDashboardData dashboard = new MockDashboardData();

    @Override
    public void start(Stage primaryStage) {
        Button customerButton = new Button("Open Customer Dashboard");
        Button employeeButton = new Button("Open Employee Dashboard");
        Button managerButton = new Button("Open Manager Dashboard");

        customerButton.setOnAction(e -> openCustomerDashboard());
        employeeButton.setOnAction(e -> openEmployeeDashboard());
        managerButton.setOnAction(e -> openManagerDashboard());

        VBox root = new VBox(15, customerButton, employeeButton, managerButton);
        root.setStyle(
                "-fx-padding: 30;" +
                        "-fx-alignment: center;" +
                        "-fx-background-color: #F6E4CE;"
        );

        primaryStage.setTitle("GSMS Dashboard Launcher");
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

    private void openCustomerDashboard() {
        CustomerDashboardView view = new CustomerDashboardView(dashboard);

        Stage stage = new Stage();
        stage.setTitle("Customer Dashboard");
        stage.setScene(new Scene(view.build(), 900, 600));
        stage.show();
    }

    private void openEmployeeDashboard() {
        EmployeeDashboardView view = new EmployeeDashboardView(dashboard);

        Stage stage = new Stage();
        stage.setTitle("Employee Dashboard");
        stage.setScene(new Scene(view.build(), 900, 600));
        stage.show();
    }

    private void openManagerDashboard() {
        ManagerDashboardView view = new ManagerDashboardView(dashboard);

        Stage stage = new Stage();
        stage.setTitle("Manager Dashboard");
        stage.setScene(new Scene(view.build(), 1000, 650));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}