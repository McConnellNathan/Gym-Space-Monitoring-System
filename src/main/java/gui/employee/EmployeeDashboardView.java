package gui.employee;

import gui.common.DashboardAlert;
import dashboard.DashboardGateway;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.BorderWidths;

public class EmployeeDashboardView {

    private static final String ORANGE = "#FECE6E";
    private static final String DARK_ORANGE = "#D1601E";
    private static final String TAN = "#F6E4CE";

    private final DashboardGateway dashboard;
    private final Runnable onSignOut;
    private final StackPane contentPane = new StackPane();
    private Label pageTitle;
    private Timeline alertRefreshTimer;
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private Label lastClockInLabel;
    private Label lastClockOutLabel;
    private int lastAlertCount = 0;
    private VBox notificationPanel;
    private VBox notificationList;
    private Button notificationToggleButton;
    private boolean notificationsCollapsed = false;

    public EmployeeDashboardView(DashboardGateway dashboard, Runnable onSignOut) {
        this.dashboard = dashboard;
        this.onSignOut = onSignOut;
    }

    public Parent build() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        VBox leftbar = buildSidebar();
        VBox mainArea = buildMainArea();
        VBox rightarea = buildNotificationPanel();

        root.getChildren().addAll(leftbar, mainArea, rightarea);
        HBox.setHgrow(mainArea, Priority.ALWAYS);

        showClassesPage();

        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: " + ORANGE + ";");

        Image logoImage = new Image(getClass().getResource("/gymlogo.png").toExternalForm());
        ImageView logo = new ImageView(logoImage);

        logo.setFitHeight(100);
        logo.setPreserveRatio(true);

        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 5, 0));

        Button classesButton = navButton("▣  Classes");
        Button occupancyButton = navButton("↗  Occupancy");
        Button timesheetButton = navButton("◷  Daily Timesheet");

        classesButton.setOnAction(e -> showClassesPage());
        occupancyButton.setOnAction(e -> showOccupancyPage());
        timesheetButton.setOnAction(e -> showTimesheetPage());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button settingsButton = navButton("⚙  Settings");
        Button supportButton = navButton("?  Support");
        Button signOutButton = navButton("⏻  Sign Out");

        signOutButton.setOnAction(e -> {
            if (alertRefreshTimer != null) alertRefreshTimer.stop();
            onSignOut.run();
        });

        sidebar.getChildren().addAll(
                logoBox,
                classesButton,
                occupancyButton,
                timesheetButton,
                new Separator(),
                spacer,
                settingsButton,
                supportButton,
                signOutButton
        );

        return sidebar;
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + DARK_ORANGE + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #dcecff;" +
                                "-fx-text-fill: " + DARK_ORANGE + ";" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 4;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + DARK_ORANGE + ";" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private VBox buildMainArea() {
        VBox main = new VBox(18);
        main.setPadding(new Insets(25));
        main.setStyle("-fx-background-color: " + TAN + ";");

        pageTitle = new Label("Hello, Employee");
        pageTitle.setTextFill(Color.web("#6B4A3A"));
        pageTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        contentPane.setPadding(new Insets(15));
        contentPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #6B4A3A;" +
                        "-fx-border-width: 3;"
        );

        main.getChildren().addAll(pageTitle, contentPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        return main;
    }

    private void showClassesPage() {
        String name = dashboard.getCurrentEmployeeName();
        pageTitle.setText(name.isEmpty() ? "Hello, Employee" : "Hello, " + name);

        VBox page = new VBox(18);
        page.setPadding(new Insets(10));

        Label heading = new Label("Upcoming Classes");
        heading.setTextFill(Color.web(DARK_ORANGE));
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox dateRow = new HBox(14);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            String dateStr = day.getDayOfMonth() + "\n" + day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Label dateBox = new Label(dateStr);
            dateBox.setAlignment(Pos.CENTER);
            dateBox.setPrefSize(55, 55);
            dateBox.setStyle(
                    "-fx-border-color: #6B4A3A;" +
                            "-fx-border-width: 3;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #6B4A3A;"
            );
            dateRow.getChildren().add(dateBox);
        }

        VBox classList = new VBox(12);
        for (String classInfo : dashboard.getClassSchedule()) {
            classList.getChildren().add(classRow(classInfo));
        }

        page.getChildren().addAll(heading, dateRow, classList);
        contentPane.getChildren().setAll(page);
    }

    private HBox classRow(String classInfo) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setStyle("-fx-border-color: transparent transparent #6B4A3A transparent;");

        Label classLabel = new Label(classInfo);
        classLabel.setTextFill(Color.web(DARK_ORANGE));
        classLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(classLabel, spacer);
        return row;
    }

    // Making occupancy page visible
    private void showOccupancyPage() {
        pageTitle.setText("Gym Occupancy");

        VBox page = new VBox(20);
        page.setAlignment(Pos.CENTER);

        int current = dashboard.getCurrentOccupancy();
        int max = dashboard.getMaxOccupancy();

        Label occupancy = new Label(current + " / " + max);
        occupancy.setTextFill(Color.web(DARK_ORANGE));
        occupancy.setStyle("-fx-font-size: 54px; -fx-font-weight: bold;");

        ProgressBar bar = new ProgressBar((double) current / max);
        bar.setPrefWidth(350);

        Label description = new Label("Current gym capacity");
        description.setTextFill(Color.web("#6B4A3A"));
        description.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        page.getChildren().addAll(occupancy, bar, description);
        contentPane.getChildren().setAll(page);
    }

    // Making timesheet page visible
    private void showTimesheetPage() {
        pageTitle.setText("Employee Timesheet");

        VBox page = new VBox(20);
        page.setPadding(new Insets(20));
        page.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Clock In / Clock Out");
        title.setTextFill(Color.web(DARK_ORANGE));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button clockInButton = new Button("Clock In");
        Button clockOutButton = new Button("Clock Out");

        clockInButton.setStyle(
                "-fx-background-color: " + ORANGE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 22;"
        );

        clockOutButton.setStyle(
                "-fx-background-color: " + DARK_ORANGE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 22;"
        );

        HBox buttonRow = new HBox(15, clockInButton, clockOutButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        lastClockInLabel = new Label("Last clock in: --");
        lastClockOutLabel = new Label("Last clock out: --");

        lastClockInLabel.setTextFill(Color.web("#6B4A3A"));
        lastClockOutLabel.setTextFill(Color.web("#6B4A3A"));

        lastClockInLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lastClockOutLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        clockInButton.setOnAction(e -> {
            String time = LocalDateTime.now().format(timeFormatter);
            lastClockInLabel.setText("Last clock in: " + time);
        });

        clockOutButton.setOnAction(e -> {
            String time = LocalDateTime.now().format(timeFormatter);
            lastClockOutLabel.setText("Last clock out: " + time);
        });

        VBox timeInfoBox = new VBox(8, lastClockInLabel, lastClockOutLabel);
        timeInfoBox.setPadding(new Insets(15, 0, 0, 0));

        page.getChildren().addAll(title, buttonRow, timeInfoBox);

        contentPane.getChildren().setAll(page);
    }

    private VBox buildNotificationPanel() {
        notificationPanel = new VBox(10);
        notificationPanel.setPadding(new Insets(12));
        notificationPanel.setStyle(
                "-fx-background-color: #F6E4CE;" +
                        "-fx-border-color: #1B3955;" +
                        "-fx-border-width: 0 0 3 0;"
        );

        Label title = new Label("Alerts");
        title.setTextFill(Color.web("#1B3955"));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        notificationToggleButton = new Button("Hide");
        notificationToggleButton.setOnAction(e -> toggleNotifications());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshNotifications());

        HBox header = new HBox(10, title, refreshButton, notificationToggleButton);
        header.setAlignment(Pos.CENTER_LEFT);

        notificationList = new VBox(8);

        notificationPanel.getChildren().addAll(header, notificationList);

        refreshNotifications();

        alertRefreshTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshNotifications()));
        alertRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        alertRefreshTimer.play();

        return notificationPanel;
    }

    private void toggleNotifications() {
        notificationsCollapsed = !notificationsCollapsed;

        notificationList.setVisible(!notificationsCollapsed);
        notificationList.setManaged(!notificationsCollapsed);

        notificationToggleButton.setText(notificationsCollapsed ? "Show" : "Hide");
    }

    private void refreshNotifications() {
        List<DashboardAlert> alerts = dashboard.getActiveAlerts();

        if (alerts.size() > lastAlertCount && notificationsCollapsed) {
            notificationsCollapsed = false;
            notificationList.setVisible(true);
            notificationList.setManaged(true);
            notificationToggleButton.setText("Hide");
        }

        lastAlertCount = alerts.size();

        notificationList.getChildren().clear();

        for (DashboardAlert alert : alerts) {
            notificationList.getChildren().add(buildAlertCard(alert));
        }
    }


    private VBox buildAlertCard(DashboardAlert alert) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10));
        card.setStyle(baseCardStyle(getAlertColor(alert), 2));

        card.setBorder(new Border(new BorderStroke(
                Color.web(getAlertColor(alert)),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(2)
        )));

        Label title = new Label(alert.getSeverity() + ": " + alert.getTitle());
        title.setTextFill(Color.web(getAlertColor(alert)));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label details = new Label(alert.getLocation() + " • " + alert.getTime());
        details.setTextFill(Color.web("#1B3955"));

        card.getChildren().addAll(title, details);

        card.setOnMouseClicked(e -> openAlertDetails(alert));

        if (alert.getSeverity() == DashboardAlert.Severity.CRITICAL) {
            addCriticalPulse(card, getAlertColor(alert));
        }

        return card;
    }

    private void addCriticalPulse(VBox card, String colorHex) {
        Color borderColor = Color.web(colorHex);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e ->
                        card.setBorder(new Border(new BorderStroke(
                                borderColor,
                                BorderStrokeStyle.SOLID,
                                new CornerRadii(10),
                                new BorderWidths(2)
                        )))
                ),
                new KeyFrame(Duration.seconds(0.25), e ->
                        card.setBorder(new Border(new BorderStroke(
                                borderColor,
                                BorderStrokeStyle.SOLID,
                                new CornerRadii(10),
                                new BorderWidths(5)
                        )))
                )
        );

        timeline.setAutoReverse(true);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void openAlertDetails(DashboardAlert alert) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Alert Details");

        Label title = new Label(alert.getSeverity() + ": " + alert.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        title.setTextFill(Color.web(getAlertColor(alert)));

        Label location = new Label("Location: " + alert.getLocation());
        Label time = new Label("Time: " + alert.getTime());
        Label description = new Label(alert.getDescription());
        description.setWrapText(true);

        VBox content = new VBox(10, title, location, time, description);
        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);

        ButtonType resolveButton = new ButtonType("Resolve", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(resolveButton, cancelButton);

        dialog.showAndWait().ifPresent(result -> {
            if (result == resolveButton) {
                handleResolve(alert);
            }
        });
    }

    private void handleResolve(DashboardAlert alert) {
        boolean resolved;

        if (alert.getSeverity() == DashboardAlert.Severity.CRITICAL) {
            TextInputDialog pinDialog = new TextInputDialog();
            pinDialog.setTitle("Manager PIN Required");
            pinDialog.setHeaderText("Critical alerts require manager override.");
            pinDialog.setContentText("Enter manager PIN:");

            resolved = pinDialog.showAndWait()
                    .map(pin -> dashboard.resolveCriticalAlert(alert.getId(), pin))
                    .orElse(false);
        } else {
            resolved = dashboard.resolveAlert(alert.getId());
        }

        Alert resultAlert = new Alert(resolved
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.ERROR);

        resultAlert.setHeaderText(null);
        resultAlert.setContentText(resolved
                ? "Alert resolved."
                : "Alert could not be resolved.");

        resultAlert.showAndWait();

        refreshNotifications();
    }

    private String getAlertColor(DashboardAlert alert) {
        return switch (alert.getSeverity()) {
            case INFO -> "#5786BC";
            case WARNING -> "#8A5A18";
            case CRITICAL -> "#B3261E";
        };
    }

    private String baseCardStyle(String color, int borderWidth) {
        return
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-cursor: hand;";
    }

}