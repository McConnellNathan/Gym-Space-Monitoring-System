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
        DashboardGateway employeeDashboard;
        try {
            dashboard = new RealDashboardData();
            employeeDashboard = new RealDashboardData();
            System.out.println("Connected to real backend dashboard data.");
        } catch (Exception e) {
            System.err.println("Falling back to mock dashboard data: " + e.getMessage());
            dashboard = new MockDashboardData();
            employeeDashboard = new MockDashboardData();
        }

        String mode = getParameters().getNamed().getOrDefault("mode", "login");

        switch (mode) {
            case "employee" -> {
                var status = employeeDashboard.signIn("krouzaud", "1234");
                if (status == null) {
                    System.err.println("[MainGuiApp] Employee sign-in failed; alert resolution will be unavailable.");
                }
                EmployeeDashboardView view = new EmployeeDashboardView(employeeDashboard, primaryStage::close);
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