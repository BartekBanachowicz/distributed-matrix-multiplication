package put.poznan.guiclient;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
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
        Socket clientSocket = new Socket(this.IPAddress, this.portNumber);

        CommunicationThread commThreadClass = new CommunicationThread(clientSocket, queue);
        Thread commThread = new Thread(commThreadClass);
        commThread.start();

        StatusThread statusThreadClass = new StatusThread(queue);
        Thread statusThread = new Thread(statusThreadClass);
        statusThread.start();
    }

}
