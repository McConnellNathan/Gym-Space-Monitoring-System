package gui.register;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TitleCard {
    public Node getContent() {
        Label title = new Label("Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#F6E4CE"));

        VBox box = new VBox(6, title);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}