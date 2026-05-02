package gui;

import gui.common.MockDashboardData;
import gui.customer.CustomerDashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Running the gui
 */
public class MainGuiApp extends Application {
    @Override
    public void start(Stage stage) {
        CustomerDashboardView customerDashboard =
                new CustomerDashboardView(new MockDashboardData());

        Scene scene = new Scene(customerDashboard.build(), 700, 500);

        stage.setTitle("GSMS - Customer Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
