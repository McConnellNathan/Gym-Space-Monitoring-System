package gui;

import dashboard.DashboardGateway;
import dashboard.MockDashboardData;
import dashboard.RealDashboardData;
import gui.customer.CustomerDashboardView;
import gui.employee.EmployeeDashboardView;
import gui.register.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainGuiApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DashboardGateway dashboard;
        try {
            dashboard = new RealDashboardData();
            System.out.println("Connected to real backend dashboard data.");
        } catch (Exception e) {
            System.err.println("Falling back to mock dashboard data: " + e.getMessage());
            dashboard = new MockDashboardData();
        }

        String mode = getParameters().getNamed().getOrDefault("mode", "login");

        switch (mode) {
            case "employee" -> {
                EmployeeDashboardView view = new EmployeeDashboardView(dashboard, primaryStage::close);
                primaryStage.setTitle("GSMS - Employee Dashboard");
                primaryStage.setScene(new Scene(view.build(), 900, 600));
            }
            case "customer" -> {
                CustomerDashboardView view = new CustomerDashboardView(dashboard, primaryStage::close);
                primaryStage.setTitle("GSMS - Customer Dashboard");
                primaryStage.setScene(new Scene(view.build(), 900, 600));
            }
            default -> {
                LoginView loginView = new LoginView(dashboard, primaryStage);
                primaryStage.setTitle("GSMS - Sign In");
                primaryStage.setScene(new Scene(loginView.getContent(), 800, 600));
            }
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}