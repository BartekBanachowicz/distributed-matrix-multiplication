package put.poznan.guiclient;

import javafx.fxml.FXML;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class GUIConverter {

    @FXML
    private static Text serverStatusText;

    @FXML
    private static Circle serverStatusDiode;

    @FXML
    public static void setConnected(){
        serverStatusDiode.setFill(Paint.valueOf("#1f4862"));
        serverStatusText.setText("Connected");
    }

}
