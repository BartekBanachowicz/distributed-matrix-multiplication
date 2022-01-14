package put.poznan.guiclient;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConnectionHandler {
    private int portNumber;
    private String IPAddress;
    private BlockingQueue<String[]> queue = new ArrayBlockingQueue<String[]>(20);

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
        System.out.println("Setting up connection to: "+getIPAddress()+":"+getPortNumber());
        Socket clientSocket = new Socket(this.IPAddress, this.portNumber);

        SenderThread sendThreadClass = new SenderThread(clientSocket, queue);
        Thread sendThread = new Thread(sendThreadClass);
        sendThread.start();

        ReceiverThread receiverThreadClass = new ReceiverThread(clientSocket, queue);
        Thread receiverThread = new Thread(receiverThreadClass);
        receiverThread.start();

        StatusThread statusThreadClass = new StatusThread(queue);
        Thread statusThread = new Thread(statusThreadClass);
        statusThread.start();
    }

}
