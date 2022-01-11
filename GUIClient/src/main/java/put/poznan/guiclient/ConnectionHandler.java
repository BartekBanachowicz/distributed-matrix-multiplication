package put.poznan.guiclient;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionHandler {
    private int portNumber;
    private String IPAddress;
    private BlockingQueue<String[]> blockingQueue;

    public void setConnectionParams(int xPortNumber, String xIPAddress){
        this.portNumber = xPortNumber;
        this.IPAddress = xIPAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void establishConnection() throws IOException {
        System.out.println("Before clientSocket creation");
        Socket clientSocket = new Socket(this.IPAddress, this.portNumber);
        System.out.println("After");
        blockingQueue = new LinkedBlockingDeque<>();

        CommunicationThread commThreadClass = new CommunicationThread(clientSocket, this.blockingQueue);
        Thread commThread = new Thread(commThreadClass);
        commThread.start();

        StatusThread statusThreadClass = new StatusThread(this.blockingQueue);
        Thread statusThread = new Thread(statusThreadClass);
        statusThread.start();
    }

    public void writeToQueue(String[] message){
        this.blockingQueue.add(message);
    }

}
