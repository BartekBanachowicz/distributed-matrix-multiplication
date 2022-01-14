package put.poznan.guiclient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class GUIClientController {
    private ConnectionHandler connectionHandler = GUIClient.getConnectionHandler();
    private DataHandler dataHandler = GUIClient.getDataHandler();
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
    private TextField matrixSizeField;

    @FXML
    private Button generateButton;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onConnectButtonClick() throws IOException {
        if(serverAddressField.getText().matches("[0-9]+.[0-9]+.[0-9]+.[0-9]+:[0-9]+")){
            String[] address = serverAddressField.getText().split(":");
            connectionHandler.setConnectionParams(Integer.parseInt(address[1]), address[0]);

            HelperThread helperThreadClass = new HelperThread(dataHandler, connectionHandler, "CONNECT");
            Thread helperThread = new Thread(helperThreadClass);
            helperThread.start();
        }
        else{
            serverAddressField.setStyle("-fx-border-color: #bb3e03");
        }
    }

    @FXML
    protected void onWriteToNewMatrixSizeField(){
        if(matrixSizeField.getLength() != 0 && matrixSizeField.getText().matches("[0-9]+")){
            dataHandler.setNewMatrixSize(Integer.parseInt(matrixSizeField.getText()));
        }
        else{
            matrixSizeField.setText("");
        }
    }

    @FXML
    protected void onWriteToNewMatrixDirectoryField(){
        if(newMatrixDirectory.getLength() != 0){
            dataHandler.setNewMatrixPath(Path.of(newMatrixDirectory.getText()));
        }
    }

    @FXML
    protected void onGenerateButtonClick(){
        if(dataHandler.getNewMatrixPath() == null){
            if(newMatrixDirectory.getText().length() == 0) { return; }
            else {
                dataHandler.setNewMatrixPath(Path.of(newMatrixDirectory.getText()));

                if(dataHandler.getNewMatrixSize() <= 0){
                    if(matrixSizeField.getText().length() > 0 && matrixSizeField.getText().matches("[0-9]+")){
                        dataHandler.setNewMatrixSize(Integer.parseInt(matrixSizeField.getText()));
                    }
                }
                else { return; }
            }
        }

        HelperThread helperThreadClass = new HelperThread(dataHandler, connectionHandler, "GENERATE");
        Thread helperThread = new Thread(helperThreadClass);
        helperThread.start();

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