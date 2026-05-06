package app;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;

public class DemoAiDashboardView {

    private static final String TAN      = "#F6E4CE";
    private static final String DARK_RED = "#CE3737";

    private final ImageView cameraView;
    private final OccupancyPanel occupancyPanel;
    private final AggressionPanel aggressionPanel;
    private final FallPanel fallPanel;
    private final WalkwayPanel walkwayPanel;
    private final SoundPanel soundPanel;
    private final Scene scene;
    private final VBox contentRoot;
    private final ScrollPane scrollPane;

    private final Timeline blinkTimeline;
    private final PauseTransition stopDelay;
    private boolean blinkState = false;

    public DemoAiDashboardView() {
        cameraView = new ImageView();
        cameraView.setFitWidth(420);
        cameraView.setPreserveRatio(true);
        cameraView.setSmooth(true);

        occupancyPanel  = new OccupancyPanel();
        aggressionPanel = new AggressionPanel();
        fallPanel       = new FallPanel();
        walkwayPanel    = new WalkwayPanel();
        soundPanel      = new SoundPanel();

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/gymLogo.png")));
        logoView.setFitWidth(260);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        HBox cameraRow = new HBox(30, cameraView, logoView);
        cameraRow.setAlignment(Pos.CENTER);
        cameraRow.setFillHeight(true);

        GridPane panelGrid = new GridPane();
        panelGrid.setHgap(10);
        panelGrid.setVgap(10);
        panelGrid.setAlignment(Pos.TOP_CENTER);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        panelGrid.getColumnConstraints().addAll(col1, col2);

        panelGrid.add(occupancyPanel, 0, 0);
        panelGrid.add(aggressionPanel, 1, 0);
        panelGrid.add(fallPanel, 0, 1);
        panelGrid.add(walkwayPanel, 1, 1);
        panelGrid.add(soundPanel, 0, 2, 2, 1);

        GridPane.setHgrow(occupancyPanel, Priority.ALWAYS);
        GridPane.setHgrow(aggressionPanel, Priority.ALWAYS);
        GridPane.setHgrow(fallPanel, Priority.ALWAYS);
        GridPane.setHgrow(walkwayPanel, Priority.ALWAYS);
        GridPane.setHgrow(soundPanel, Priority.ALWAYS);

        occupancyPanel.setMaxWidth(Double.MAX_VALUE);
        aggressionPanel.setMaxWidth(Double.MAX_VALUE);
        fallPanel.setMaxWidth(Double.MAX_VALUE);
        walkwayPanel.setMaxWidth(Double.MAX_VALUE);
        soundPanel.setMaxWidth(Double.MAX_VALUE);

        contentRoot = new VBox(10, cameraRow, panelGrid);
        contentRoot.setPadding(new Insets(20));
        contentRoot.setStyle("-fx-background-color: " + TAN + ";");

        scrollPane = new ScrollPane(contentRoot);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: " + TAN + "; -fx-background-color: " + TAN + ";");

        scene = new Scene(scrollPane, 1100, 850);

        blinkTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            blinkState = !blinkState;
            contentRoot.setStyle("-fx-background-color: " + (blinkState ? DARK_RED : TAN) + ";");
        }));
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);

        stopDelay = new PauseTransition(Duration.seconds(4));
        stopDelay.setOnFinished(e -> {
            blinkTimeline.stop();
            blinkState = false;
            contentRoot.setStyle("-fx-background-color: " + TAN + ";");
        });
    }

    public void setAlarmBlinking(boolean active) {
        if (active) {
            stopDelay.stop();
            blinkTimeline.play();
        } else {
            stopDelay.playFromStart();
        }
    }

    public Scene getScene()                     { return scene; }
    public ImageView getCameraView()            { return cameraView; }
    public OccupancyPanel getOccupancyPanel()   { return occupancyPanel; }
    public AggressionPanel getAggressionPanel() { return aggressionPanel; }
    public FallPanel getFallPanel()             { return fallPanel; }
    public WalkwayPanel getWalkwayPanel()       { return walkwayPanel; }
    public SoundPanel getSoundPanel()           { return soundPanel; }
}