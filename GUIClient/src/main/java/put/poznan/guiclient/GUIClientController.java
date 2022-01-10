package put.poznan.guiclient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GUIClientController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}