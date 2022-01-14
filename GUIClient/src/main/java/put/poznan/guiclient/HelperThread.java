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

    private void generateData(){}

    private void connectToServer() throws IOException {
        connectionHandler.establishConnection();
    }

    private void startProcessing() throws FileNotFoundException {
        dataHandler.addLeftMatrix();
        dataHandler.addRightMatrix();
        dataHandler.initializeResultMatrix();
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
        }
    }
}
