package app;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DemoAiDashboardView {

    private static final String TAN     = "#F6E4CE";
    private static final String DARK_RED = "#CE3737";

    private final ImageView cameraView;
    private final OccupancyPanel occupancyPanel;
    private final AggressionPanel aggressionPanel;
    private final FallPanel fallPanel;
    private final WalkwayPanel walkwayPanel;
    private final SoundPanel soundPanel;
    private final Scene scene;
    private final VBox root;

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
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        HBox cameraRow = new HBox(60, cameraView, logoView);
        cameraRow.setAlignment(Pos.CENTER_LEFT);

        root = new VBox(14, cameraRow, occupancyPanel, aggressionPanel, fallPanel, walkwayPanel, soundPanel);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + TAN + ";");

        scene = new Scene(root, 900, 1175);

        blinkTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            blinkState = !blinkState;
            root.setStyle("-fx-background-color: " + (blinkState ? DARK_RED : TAN) + ";");
        }));
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);

        stopDelay = new PauseTransition(Duration.seconds(4));
        stopDelay.setOnFinished(e -> {
            blinkTimeline.stop();
            blinkState = false;
            root.setStyle("-fx-background-color: " + TAN + ";");
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
