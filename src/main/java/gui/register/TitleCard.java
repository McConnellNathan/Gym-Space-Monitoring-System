package gui.register;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TitleCard {
    public Node getContent() {
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/gymLogo.png")));
        logo.setPreserveRatio(true);

        Label title = new Label("Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        title.setTextFill(Color.web("#F6E4CE"));

        VBox box = new VBox(10, logo, title);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);

        logo.fitWidthProperty().bind(box.widthProperty().multiply(0.8));

        return box;
    }
}
