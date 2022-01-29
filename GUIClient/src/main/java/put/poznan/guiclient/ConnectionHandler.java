package put.poznan.guiclient;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionHandler {
    private int portNumber;
    private String IPAddress;
    private final BlockingQueue<String[]> sendQueue = new ArrayBlockingQueue<>(20);
    private final BlockingQueue<String[]> statusQueue = new ArrayBlockingQueue<>(20);
    private final BlockingQueue<String> dataQueue = new ArrayBlockingQueue<>(20);

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

    public void closeApp() throws InterruptedException {
        String[] messageArr = new String[1];
        messageArr[0] = "STOP";
        String messageStr = "STOP";
        sendQueue.put(messageArr);
        statusQueue.put(messageArr);
        dataQueue.put(messageStr);
    }

    public void abort() throws InterruptedException {
        String[] messageArr = new String[1];
        messageArr[0] = "ABORT";
        String messageStr = "STOP";
        sendQueue.put(messageArr);
        dataQueue.put(messageStr);
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

    public void readServerData(DataHandler dataHandler) throws IOException {
        ReceiverThread recThreadClass = new ReceiverThread(sendQueue, dataQueue, dataHandler);
        Thread recThread = new Thread(recThreadClass);
        recThread.start();
    }

}
