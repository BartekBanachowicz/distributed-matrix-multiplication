package put.poznan.guiclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunicationThread implements Runnable{
    private Socket clientSocket;
    private BlockingQueue<String[]> blockingQueue;
    private Boolean continueProcessing = true;
    private InputStream inputStream;
    private OutputStream outputStream;

    CommunicationThread(Socket xClientSocket, BlockingQueue<String[]> xBlockingQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.blockingQueue = xBlockingQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();

    }

    @Override
    public void run() {

        String[] message = new String[0];

        while(continueProcessing){
            try {
                message = this.blockingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(message[0].contains("GET")){
                try {
                    outputStream.write(message[0].getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(message[0].contains("POST")){
                try {
                    outputStream.write(message[0].getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
