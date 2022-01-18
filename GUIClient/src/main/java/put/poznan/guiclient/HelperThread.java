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

        String[] messageLeft = new String[2];
        messageLeft[0] = "POST LEFT-MATRIX\n";
        messageLeft[1] = dataHandler.getLeftMatrixAsString();
        System.out.println(messageLeft[0]);
        System.out.println(messageLeft[1]);
        connectionHandler.writeToQueue(messageLeft);

        System.out.println("Left sent");

        String[] messageRight = new String[2];
        messageRight[0] = "POST RIGHT-MATRIX\n";
        messageRight[1] = dataHandler.getRightMatrixAsString();
        System.out.println(messageRight[0]);
        System.out.println(messageRight[1]);
        connectionHandler.writeToQueue(messageRight);

        System.out.println("Right sent");
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
                            break;
            case "GENERATE":
                            System.out.println("What am I doing here?");
                            try {
                                this.generateData();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
        }
    }
}
