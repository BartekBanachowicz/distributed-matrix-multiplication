package put.poznan.guiclient;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HelperThread implements Runnable {
    private DataHandler dataHandler;
    private ConnectionHandler connectionHandler;
    private String operation;
    private GUIClientController controller;

    HelperThread(DataHandler xDataHandler, ConnectionHandler xConnectionHandler, String xOperation, GUIClientController controller){
        this.dataHandler = xDataHandler;
        this.connectionHandler = xConnectionHandler;
        this.operation = xOperation;
        this.controller = controller;
    }

    private void collectData() throws IOException {
        connectionHandler.readServerData(dataHandler);
    }

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

        String[] messageLeft = new String[2];
        messageLeft[0] = "POST LEFT-MATRIX";
        messageLeft[1] = dataHandler.getLeftMatrixAsString();
        System.out.println("Left matrix in queue");
        connectionHandler.writeToQueue(messageLeft);

        String[] messageRight = new String[2];
        messageRight[0] = "POST RIGHT-MATRIX";
        messageRight[1] = dataHandler.getRightMatrixAsString();
        System.out.println("Right matrix in queue");
        connectionHandler.writeToQueue(messageRight);
    }

    @Override
    public void run() {
        switch (this.operation){
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

                            try {
                                this.collectData();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
            case "GENERATE":
                            try {
                                this.generateData();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
        }
    }
}
