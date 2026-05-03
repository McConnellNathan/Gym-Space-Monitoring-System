package gui.manager;

import datastore.MachineData;
import gui.common.DashboardAlert;
import gui.common.DashboardGateway;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ManagerDashboardView {

    private static final String DARK_RED = "#CE3737";
    private static final String RED = "#70191D";
    private static final String TAN = "#F6E4CE";

    private final DashboardGateway dashboard;
    private final Runnable onSignOut;
    private final StackPane contentPane = new StackPane();
    private Label pageTitle;
    private VBox notificationPanel;
    private VBox notificationList;
    private Button notificationToggleButton;
    private boolean notificationsCollapsed = false;
    private int lastAlertCount = 0;
    private Timeline alertRefreshTimer;
    private final java.time.format.DateTimeFormatter timeFormatter =
            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public ManagerDashboardView(DashboardGateway dashboard, Runnable onSignOut) {
        this.dashboard = dashboard;
        this.onSignOut = onSignOut;
    }

    public Parent build() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        VBox sidebar = buildSidebar();
        VBox mainArea = buildMainArea();
        VBox alertPanel = buildNotificationPanel();

        root.getChildren().addAll(sidebar, mainArea, alertPanel);
        HBox.setHgrow(mainArea, Priority.ALWAYS);

        showDashboardPage();

        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: " + RED + ";");

        Label logo = new Label("●");
        logo.setTextFill(Color.web(DARK_RED));
        logo.setStyle("-fx-font-size: 52px;");

        Button dashboardButton = navButton("▣  Dashboard");
        Button equipmentButton = navButton("▣  Equipment");
        Button occupancyButton = navButton("↗  Occupancy");
        Button logsButton = navButton("▣  Alert Logs");

        dashboardButton.setOnAction(e -> showDashboardPage());
        equipmentButton.setOnAction(e -> showEquipmentPage());
        occupancyButton.setOnAction(e -> showOccupancyPage());
        logsButton.setOnAction(e -> showAlertLogsPage());

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
                logo,
                dashboardButton,
                equipmentButton,
                occupancyButton,
                logsButton,
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
                        "-fx-text-fill: " + DARK_RED + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #dcecff;" +
                                "-fx-text-fill: " + DARK_RED + ";" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 4;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + DARK_RED + ";" +
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

        pageTitle = new Label("Hello, Manager");
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
        pageTitle.setText(name.isEmpty() ? "Hello, Manager" : "Hello, " + name);

        VBox page = new VBox(18);
        page.setPadding(new Insets(10));

        Label heading = new Label("Upcoming Classes");
        heading.setTextFill(Color.web(DARK_RED));
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
        classLabel.setTextFill(Color.web(DARK_RED));
        classLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button bookButton = new Button("BOOK");
        bookButton.setStyle(
                "-fx-background-color: " + RED + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 18;"
        );

        bookButton.setOnAction(e -> {
            boolean success = dashboard.registerForClass(classInfo);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(success
                    ? "Successfully registered for: " + classInfo
                    : "This class is full.");
            alert.showAndWait();
        });

        row.getChildren().addAll(classLabel, spacer, bookButton);
        return row;
    }

    private void showOccupancyPage() {
        pageTitle.setText("Gym Occupancy");

        VBox page = new VBox(20);
        page.setAlignment(Pos.CENTER);

        int current = dashboard.getCurrentOccupancy();
        int max = dashboard.getMaxOccupancy();

        Label occupancy = new Label(current + " / " + max);
        occupancy.setTextFill(Color.web(DARK_RED));
        occupancy.setStyle("-fx-font-size: 54px; -fx-font-weight: bold;");

        ProgressBar bar = new ProgressBar((double) current / max);
        bar.setPrefWidth(350);

        Label description = new Label("Current gym capacity");
        description.setTextFill(Color.web("#6B4A3A"));
        description.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        page.getChildren().addAll(occupancy, bar, description);
        contentPane.getChildren().setAll(page);
    }

    private void showTimesheetPage() {
        pageTitle.setText("Daily Timesheet");

        VBox page = new VBox(20);
        page.setPadding(new Insets(20));

        Label title = new Label("Traffic Overview");
        title.setTextFill(Color.web(DARK_RED));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        String[] options = {"Daily", "Weekly", "Monthly", "Yearly"};
        ToggleGroup group = new ToggleGroup();

        VBox buttons = new VBox(15);
        for (String option : options) {
            RadioButton radio = new RadioButton(option);
            radio.setToggleGroup(group);
            radio.setTextFill(Color.web("#6B4A3A"));
            radio.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            buttons.getChildren().add(radio);
        }

        Label placeholder = new Label("Traffic chart will display here.");
        placeholder.setTextFill(Color.web(DARK_RED));
        placeholder.setStyle("-fx-font-size: 18px;");

        page.getChildren().addAll(title, placeholder, buttons);
        contentPane.getChildren().setAll(page);
    }

    private VBox buildNotificationPanel() {
        notificationPanel = new VBox(10);
        notificationPanel.setPadding(new Insets(12));
        notificationPanel.setPrefWidth(300);
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

    private void showAlertLogsPage() {
        pageTitle.setText("Alert Resolution Logs");

        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label heading = new Label("Resolved Alerts");
        heading.setTextFill(Color.web(DARK_RED));
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox logList = new VBox(10);

        List<DashboardAlert> logs = dashboard.getResolvedAlertLogs();

        if (logs.isEmpty()) {
            Label empty = new Label("No resolved alerts yet.");
            empty.setTextFill(Color.web("#6B4A3A"));
            empty.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            logList.getChildren().add(empty);
        } else {
            for (DashboardAlert alert : logs) {
                logList.getChildren().add(buildLogCard(alert));
            }
        }

        ScrollPane scrollPane = new ScrollPane(logList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        page.getChildren().addAll(heading, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        contentPane.getChildren().setAll(page);
    }

    private VBox buildLogCard(DashboardAlert alert) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + getAlertColor(alert) + ";" +
                        "-fx-border-width: 2;"
        );

        Label title = new Label(alert.getSeverity() + ": " + alert.getTitle());
        title.setTextFill(Color.web(getAlertColor(alert)));
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label location = new Label("Location: " + alert.getLocation());
        Label detectedTime = new Label("Detected: " + alert.getTime());
        Label resolvedTime = new Label("Resolved: " + LocalDateTime.now().format(timeFormatter));
        Label description = new Label(alert.getDescription());
        description.setWrapText(true);

        location.setTextFill(Color.web("#1B3955"));
        detectedTime.setTextFill(Color.web("#1B3955"));
        resolvedTime.setTextFill(Color.web("#1B3955"));
        description.setTextFill(Color.web("#6B4A3A"));

        card.getChildren().addAll(title, location, detectedTime, resolvedTime, description);

        return card;
    }

    private void showDashboardPage() {
        String name = dashboard.getCurrentEmployeeName();
        pageTitle.setText(name.isEmpty() ? "Manager Dashboard" : "Hello, " + name);

        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label title = new Label("Manager Overview");
        title.setTextFill(Color.web(DARK_RED));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label stats = new Label(
                "Current Occupancy: " + dashboard.getCurrentOccupancy() + " / " + dashboard.getMaxOccupancy()
                        + "\nActive Alerts: " + dashboard.getActiveAlerts().size()
        );
        stats.setTextFill(Color.web("#6B4A3A"));
        stats.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        page.getChildren().addAll(title, stats);
        contentPane.getChildren().setAll(page);
    }

    private void showEquipmentPage() {
        pageTitle.setText("Equipment Usage");

        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label title = new Label("Equipment Usage");
        title.setTextFill(Color.web(DARK_RED));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        page.getChildren().add(title);

        MachineData[] machines = dashboard.getMachineData();
        if (machines == null || machines.length == 0) {
            Label placeholder = new Label("Equipment usage and maintenance trends will display here.");
            placeholder.setTextFill(Color.web("#6B4A3A"));
            placeholder.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            page.getChildren().add(placeholder);
        } else {
            for (MachineData m : machines) {
                Label row = new Label(m.machineType() + ": " + m.usageCount() + " uses");
                row.setTextFill(Color.web("#6B4A3A"));
                row.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                page.getChildren().add(row);
            }
        }

        contentPane.getChildren().setAll(page);
    }

    private VBox buildAlertCard(DashboardAlert alert) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: " + getAlertColor(alert) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );

        Label title = new Label(alert.getSeverity() + ": " + alert.getTitle());
        title.setTextFill(Color.web(getAlertColor(alert)));
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label details = new Label(alert.getLocation() + " • " + alert.getTime());
        details.setTextFill(Color.web("#1B3955"));

        card.getChildren().addAll(title, details);

        card.setOnMouseClicked(e -> openAlertDetails(alert));

        return card;
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

        Alert resultAlert = new Alert(resolved ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        resultAlert.setHeaderText(null);
        resultAlert.setContentText(resolved ? "Alert resolved." : "Alert could not be resolved.");
        resultAlert.showAndWait();

        refreshNotifications();
        showAlertLogsPage();
    }

    private String getAlertColor(DashboardAlert alert) {
        return switch (alert.getSeverity()) {
            case INFO -> "#5786BC";
            case WARNING -> "#8A5A18";
            case CRITICAL -> "#B3261E";
        };
    }
}