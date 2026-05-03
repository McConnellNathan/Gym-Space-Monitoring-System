package gui.customer;

import dashboard.DashboardGateway;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class CustomerDashboardView {

    private static final String BLUE = "#5786BC";
    private static final String DARK_BLUE = "#1B3955";
    private static final String TAN = "#F6E4CE";

    private final DashboardGateway dashboard;
    private final Runnable onSignOut;
    private final StackPane contentPane = new StackPane();
    private Label pageTitle;

    public CustomerDashboardView(DashboardGateway dashboard, Runnable onSignOut) {
        this.dashboard = dashboard;
        this.onSignOut = onSignOut;
    }

    public Parent build() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: white;");

        VBox sidebar = buildSidebar();
        VBox mainArea = buildMainArea();

        root.getChildren().addAll(sidebar, mainArea);
        HBox.setHgrow(mainArea, Priority.ALWAYS);

        showClassesPage();

        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: " + BLUE + ";");

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

        signOutButton.setOnAction(e -> onSignOut.run());

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
                        "-fx-text-fill: " + DARK_BLUE + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #dcecff;" +
                                "-fx-text-fill: " + DARK_BLUE + ";" +
                                "-fx-font-size: 15px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 4;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + DARK_BLUE + ";" +
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

        pageTitle = new Label("Hello, Customer");
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
        pageTitle.setText("Hello, Customer");

        VBox page = new VBox(18);
        page.setPadding(new Insets(10));

        Label heading = new Label("Upcoming Classes");
        heading.setTextFill(Color.web(DARK_BLUE));
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox dateRow = new HBox(14);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        String[] dates = {"19\nSun", "20\nMon", "21\nTue", "22\nWed", "23\nThu", "24\nFri", "25\nSat"};
        for (String date : dates) {
            Label dateBox = new Label(date);
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
        classLabel.setTextFill(Color.web(DARK_BLUE));
        classLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button bookButton = new Button("BOOK");
        bookButton.setStyle(
                "-fx-background-color: " + BLUE + ";" +
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
        occupancy.setTextFill(Color.web(DARK_BLUE));
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
        title.setTextFill(Color.web(DARK_BLUE));
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
        placeholder.setTextFill(Color.web(DARK_BLUE));
        placeholder.setStyle("-fx-font-size: 18px;");

        page.getChildren().addAll(title, placeholder, buttons);
        contentPane.getChildren().setAll(page);
    }
}