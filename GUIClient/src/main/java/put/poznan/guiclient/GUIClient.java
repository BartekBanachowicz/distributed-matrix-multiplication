package put.poznan.guiclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIClient extends Application {
    private static Stage stage;
    private static final DataHandler dataHandler = new DataHandler();
    private static final ConnectionHandler connectionHandler = new ConnectionHandler();
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
        stage.setOnCloseRequest(windowEvent -> {
            try {
                connectionHandler.closeApp();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
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