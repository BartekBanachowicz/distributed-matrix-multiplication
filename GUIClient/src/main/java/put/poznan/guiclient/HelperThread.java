package put.poznan.guiclient;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HelperThread implements Runnable {
    private final DataHandler dataHandler;
    private final ConnectionHandler connectionHandler;
    private final String operation;

    HelperThread(DataHandler dataHandler, ConnectionHandler connectionHandler, String operation){
        this.dataHandler = dataHandler;
        this.connectionHandler = connectionHandler;
        this.operation = operation;
    }

    private void collectData() throws IOException {
        connectionHandler.readServerData(dataHandler);
    }

    private void generateData() throws FileNotFoundException {
        dataHandler.generateMatrixToFile();
    }

    private void connectToServer() {
        try {
            connectionHandler.establishConnection();
        } catch (IOException e) {
            GUIClient.getAdapter().setRefused();
        }
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

    private void abort() throws InterruptedException {
        connectionHandler.abort();
    }


    @Override
    public void run() {
        switch (this.operation){
            case "CONNECT":
                            this.connectToServer();
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

            case "ABORT":
                            try {
                                abort();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
        }
    }
}
