package put.poznan.guiclient;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionHandler {
    private int portNumber;
    private String IPAddress;
    private BlockingQueue<String[]> sendQueue = new ArrayBlockingQueue<String[]>(20);
    private BlockingQueue<String> statusQueue = new ArrayBlockingQueue<String>(20);
    private BlockingQueue<String> dataQueue = new ArrayBlockingQueue<String>(20);

    public void setConnectionParams(int xPortNumber, String xIPAddress){
        this.portNumber = xPortNumber;
        this.IPAddress = xIPAddress;
    }

    public void writeToQueue(String[] message){
        sendQueue.add(message);
    }


    public int getPortNumber() {
        return portNumber;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void establishConnection() throws IOException {
        System.out.println("Setting up connection to: "+getIPAddress()+":"+getPortNumber());
        Socket clientSocket = new Socket(this.IPAddress, this.portNumber);

        ConnectionThread connThreadClass = new ConnectionThread(clientSocket, sendQueue, statusQueue, dataQueue);
        Thread connThread = new Thread(connThreadClass);
        connThread.start();

        StatusThread statusThreadClass = new StatusThread(sendQueue, statusQueue);
        Thread statusThread = new Thread(statusThreadClass);
        statusThread.start();
    }

}
