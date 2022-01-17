package put.poznan.guiclient;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HelperThread implements Runnable {
    private DataHandler dataHandler;
    private ConnectionHandler connectionHandler;
    private String operation;

    HelperThread(DataHandler xDataHandler, ConnectionHandler xConnectionHandler, String xOperation){
        this.dataHandler = xDataHandler;
        this.connectionHandler = xConnectionHandler;
        this.operation = xOperation;
    }

    private void collectData(){}

    private void generateData() throws FileNotFoundException {
        dataHandler.generateMatrixToFile();
    }

    private void connectToServer() throws IOException {
        connectionHandler.establishConnection();
    }

    private void startProcessing() throws FileNotFoundException {
        dataHandler.addLeftMatrix();
        dataHandler.addRightMatrix();
        dataHandler.initializeResultMatrix();

        String[] message = new String[2];
        message[0] = "POST LEFT-MATRIX;SIZE "+dataHandler.getMatrixSize();
        message[1] = dataHandler.getLeftMatrixAsString();
        connectionHandler.writeToQueue(message);

        message[0] = "POST RIGHT-MATRIX;SIZE "+dataHandler.getMatrixSize();
        message[1] = dataHandler.getRightMatrixAsString();
        connectionHandler.writeToQueue(message);
    }

    @Override
    public void run() {
        switch (this.operation){
            case "COLLECT": this.collectData();
                            break;

            case "CONNECT":
                            try {
                                this.connectToServer();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

            case "START":
                            try {
                                this.startProcessing();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
            case "GENERATE":
                            try {
                                this.generateData();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
        }
    }
}
