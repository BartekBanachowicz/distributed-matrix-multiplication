package put.poznan.guiclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable{
    private Socket clientSocket;
    public BlockingQueue<String[]> queue;
    private Boolean continueProcessing = true;
    private InputStream inputStream;
    private OutputStream outputStream;

    SenderThread(Socket xClientSocket, BlockingQueue<String[]> xQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.queue = xQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();
    }

    @Override
    public void run() {

        String[] message = new String[0];

        while(continueProcessing){

            try {
                message = queue.take();
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
