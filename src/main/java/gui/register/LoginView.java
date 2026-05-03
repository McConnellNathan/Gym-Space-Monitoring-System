package gui.register;

import datastore.Employee;
import gui.common.DashboardGateway;
import gui.employee.EmployeeDashboardView;
import gui.manager.ManagerDashboardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LoginView {
    private static final String TAN = "#F6E4CE";
    private static final String DARK_BLUE = "#1b3955";
    private static final String ERROR_RED = "#FF6B6B";

    private final DashboardGateway dashboard;
    private final Stage stage;
    private StackPane content;

    public LoginView(DashboardGateway dashboard, Stage stage) {
        this.dashboard = dashboard;
        this.stage = stage;
        initContent();
    }

    public Parent getContent() {
        return content;
    }

    private void initContent() {
        TitleCard title = new TitleCard();
        LoginInputs inputs = new LoginInputs();
        LoginSubmit submit = new LoginSubmit();

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ERROR_RED));
        errorLabel.setVisible(false);

        Runnable onSubmit = () -> {
            String username = inputs.getUsername();
            String password = inputs.getPassword();

            Employee.EmployeeStatus status = dashboard.signIn(username, password);

            if (status == null) {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
                return;
            }

            errorLabel.setVisible(false);
            openDashboard(status);
        };

        submit.setOnSubmit(onSubmit);
        inputs.setOnEnter(onSubmit);

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(340);
        card.setBackground(new Background(new BackgroundFill(Color.web(DARK_BLUE), new CornerRadii(12), Insets.EMPTY)));
        card.getChildren().addAll(title.getContent(), inputs.getContent(), submit.getContent(), errorLabel);

        content = new StackPane(card);
        content.setBackground(new Background(new BackgroundFill(Color.web(TAN), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void openDashboard(Employee.EmployeeStatus status) {
        switch (status) {
            case MANAGER -> {
                ManagerDashboardView view = new ManagerDashboardView(dashboard);
                stage.setScene(new Scene(view.build(), 1000, 650));
                stage.setTitle("Manager Dashboard");
            }
            case EMPLOYEE -> {
                EmployeeDashboardView view = new EmployeeDashboardView(dashboard);
                stage.setScene(new Scene(view.build(), 900, 600));
                stage.setTitle("Employee Dashboard");
            }
        }
    }
}