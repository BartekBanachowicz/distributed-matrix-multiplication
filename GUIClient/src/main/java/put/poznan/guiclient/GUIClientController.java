package put.poznan.guiclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class GUIClientController implements Initializable {
    private final ConnectionHandler connectionHandler = GUIClient.getConnectionHandler();
    private final DataHandler dataHandler = GUIClient.getDataHandler();
    private final Desktop desktop = Desktop.getDesktop();

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
    private Button startProcessingButton;

    @FXML
    private Text serverStatusText;

    @FXML
    private Circle serverStatusDiode;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ListView<String> unitsList;

    @FXML
    private Button abortButton;

    @FXML
    protected void onConnectButtonClick() {
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
    protected void onStartProcessingButtonClick(){
        System.out.println("Start button clicked");
        System.out.println(dataHandler.getLeftMatrixPath()+" "+dataHandler.getRightMatrixPath()+" "+dataHandler.getResultMatrixPath());
        if(dataHandler.getLeftMatrixPath() != null
                && (new File(String.valueOf(dataHandler.getLeftMatrixPath())).exists())
                && (dataHandler.getRightMatrixPath() != null)
                && (new File(String.valueOf(dataHandler.getRightMatrixPath())).exists())
                && (dataHandler.getResultMatrixPath() != null))
        {
            System.out.println("Start thread created");
            HelperThread helperThreadClass = new HelperThread(dataHandler, connectionHandler, "START");
            Thread helperThread = new Thread(helperThreadClass);
            helperThread.start();
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
            leftMatrixDirectory.setText(file.getAbsolutePath());
            dataHandler.setLeftMatrixPath(Path.of(file.getAbsolutePath()));
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
                dataHandler.setRightMatrixPath(Path.of(file.getAbsolutePath()));
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
            dataHandler.setResultMatrixPath(Path.of(file.getAbsolutePath()));
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

    @FXML
    protected void onAbortButtonClick(){
        Platform.runLater(new HelperThread(dataHandler, connectionHandler, "ABORT"));
    }

    public void setConnected(){
        serverStatusDiode.setFill(Paint.valueOf("#46993d"));
        serverStatusText.setText("Connected: IDLE");
    }

    public void setConnecting(){
        serverStatusDiode.setFill(Paint.valueOf("#376fad"));
        serverStatusText.setText("Connecting...");
    }

    public void setReady(){
        serverStatusDiode.setFill(Paint.valueOf("#46993d"));
        serverStatusText.setText("Connected: Ready");
    }

    public void setRunning(){
        serverStatusDiode.setFill(Paint.valueOf("#fa9111"));
        serverStatusText.setText("Connected: Running");
    }

    public void setStopped(){
        serverStatusDiode.setFill(Paint.valueOf("#d40d0d"));
        serverStatusText.setText("Connected: Stopped");
    }

    public void setRefused(){
        serverStatusDiode.setFill(Paint.valueOf("#d40d0d"));
        serverStatusText.setText("Connection refused");
    }

    public void setListOfUnits(String[] units){
        Platform.runLater(() -> {
            unitsList.getItems().clear();
            for (String unit : units) {
                unitsList.getItems().add(unit);
            }
        });
    }

    public void changeProgress(double progress){
        progressBar.setProgress(progress);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}