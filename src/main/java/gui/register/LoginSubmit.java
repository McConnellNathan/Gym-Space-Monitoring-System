package gui.register;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class LoginSubmit {
    private final Button button = new Button("Sign In");

    public Node getContent() {
        String normal =
            "-fx-background-color: #F6E4CE;" +
            "-fx-text-fill: #1b3955;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 30;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14;";

        String hovered =
            "-fx-background-color: #ddc9b0;" +
            "-fx-text-fill: #1b3955;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 30;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14;";

        button.setStyle(normal);
        button.setMinWidth(120);
        button.setOnMouseEntered(e -> button.setStyle(hovered));
        button.setOnMouseExited(e -> button.setStyle(normal));

        VBox box = new VBox(button);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public void setOnSubmit(Runnable action) {
        button.setOnAction(e -> action.run());
    }
}