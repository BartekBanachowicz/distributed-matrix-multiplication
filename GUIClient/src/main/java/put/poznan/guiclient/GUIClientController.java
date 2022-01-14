package put.poznan.guiclient;

import java.awt.Desktop;
import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import put.poznan.guiclient.GUIClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class GUIClientController {
    private DataHandler dataHandler = new DataHandler();
    private ConnectionHandler connectionHandler = new ConnectionHandler();
    private Desktop desktop = Desktop.getDesktop();

    /*private EventHandler<ActionEvent> browserButtonEventHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(final ActionEvent actionEvent) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open resource file");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("csv", "*.csv")
            );
        }
    }*/


    @FXML
    private Label welcomeText;

    @FXML
    private TextField serverAddressField;

    @FXML
    private TextField leftMatrixDirectory;

    @FXML
    private TextField rightMatrixDirectory;

    @FXML
    private TextField resultMatrixDirectory;

    @FXML
    private TextField newMatrixDirectory;

    @FXML
    private Button leftMatrixButton;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onConnectButtonClick() throws IOException {
        if(serverAddressField.getText().matches("[0-9]+.[0-9]+.[0-9]+.[0-9]+:[0-9]+")){
            String[] address = serverAddressField.getText().split(":");
            connectionHandler.setConnectionParams(Integer.parseInt(address[1]), address[0]);
            connectionHandler.establishConnection();
        }
        else{
            serverAddressField.setStyle("-fx-border-color: #bb3e03");
        }
    }

    @FXML
    protected void onBrowserButtonClick_left(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open resource file");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("csv", "*.csv")
        );
        try{
            File file = fileChooser.showOpenDialog(GUIClient.getStage());
            if(file.exists()){
                leftMatrixDirectory.setText(file.getAbsolutePath());
            }
        }catch(NullPointerException exception){
            System.out.println("NullPointerException: Did not choose file.");
        }
    }

    @FXML
    protected void onBrowserButtonClick_right(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open resource file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("csv", "*.csv")
        );
        try{
            File file = fileChooser.showOpenDialog(GUIClient.getStage());
            if(file.exists()){
                rightMatrixDirectory.setText(file.getAbsolutePath());
            }
        }catch(NullPointerException exception){
            System.out.println("NullPointerException: Did not choose file.");
        }
    }

    @FXML
    protected void onBrowserButtonClick_result(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open resource file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("csv", "*.csv")
        );
        try{
            File file = fileChooser.showSaveDialog(GUIClient.getStage());
            resultMatrixDirectory.setText(file.getAbsolutePath());
        }catch(NullPointerException exception){
            System.out.println("NullPointerException: Did not choose file.");
        }
    }

    @FXML
    protected void onBrowserButtonClick_new(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open resource file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("csv", "*.csv")
        );
        try{
            File file = fileChooser.showSaveDialog(GUIClient.getStage());
            newMatrixDirectory.setText(file.getAbsolutePath());
        }catch(NullPointerException exception){
            System.out.println("NullPointerException: Did not choose file.");
        }
    }





}