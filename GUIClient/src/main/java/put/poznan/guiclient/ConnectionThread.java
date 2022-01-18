package put.poznan.guiclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class ConnectionThread implements Runnable{
    private Socket clientSocket;
    public BlockingQueue<String[]> queue;
    private Boolean continueProcessing = true;
    private InputStream inputStream;
    private OutputStream outputStream;

    ConnectionThread(Socket xClientSocket, BlockingQueue<String[]> xQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.queue = xQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();
    }

    @Override
    public void run() {

        String[] message = new String[0];

        try {
            outputStream.write("POST REGISTER;TYPE CLIENT\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(continueProcessing) {

            try {
                message = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (message[0].contains("GET")) {
                try {
                    outputStream.write(message[0].getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message[0].contains("POST")) {
                try {
                    System.out.println(message[0]);
                    System.out.println(message[1]);
                    outputStream.write(message[0].getBytes());
                    outputStream.write(message[1].getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] buffer = new byte[20000];
            try {
                int validData = inputStream.read(buffer);
                System.out.println(new String(buffer, 0, validData-1, StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
