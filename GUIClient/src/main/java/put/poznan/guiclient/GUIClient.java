package put.poznan.guiclient;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;


import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class GUIClient extends Application {
    private static Stage stage;
    private static DataHandler dataHandler = new DataHandler();
    private static ConnectionHandler connectionHandler = new ConnectionHandler();
    private static GUIAdapter adapter;

    @Override
    public void start(Stage xStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUIClient.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 480);
        adapter = new GUIAdapter(fxmlLoader.getController());
        stage = xStage;
        stage.setTitle("Matrix multiplication");
        stage.setScene(scene);
        stage.show();
    }


    public static Stage getStage() {
        return stage;
    }

    public static DataHandler getDataHandler(){
        return dataHandler;
    }

    public static ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public static GUIAdapter getAdapter() {
        return adapter;
    }

    public static void main(String[] args) {
        launch();
    }
}