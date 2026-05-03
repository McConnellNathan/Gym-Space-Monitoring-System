package gui.register;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginInputs {
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    public Node getContent() {
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(10, usernameField, passwordField);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return passwordField.getText(); }

    public void setOnEnter(Runnable action) {
        passwordField.setOnAction(e -> action.run());
    }
}