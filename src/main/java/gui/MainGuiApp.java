package gui;

import gui.common.DashboardGateway;
import gui.common.MockDashboardData;
import gui.common.RealDashboardData;
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

        LoginView loginView = new LoginView(dashboard, primaryStage);
        primaryStage.setTitle("GSMS - Sign In");
        primaryStage.setScene(new Scene(loginView.getContent(), 600, 450));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}