package put.poznan.guiclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class ConnectionThread implements Runnable{
    private Socket clientSocket;
    private BlockingQueue<String[]> sendQueue;
    private BlockingQueue<String> statusQueue;
    private BlockingQueue<String> dataQueue;
    private Boolean continueProcessing = true;
    private InputStream inputStream;
    private OutputStream outputStream;

    ConnectionThread(Socket xClientSocket, BlockingQueue<String[]> sendQueue, BlockingQueue<String> statusQueue,
                     BlockingQueue<String> dataQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.sendQueue = sendQueue;
        this.statusQueue = statusQueue;
        this.dataQueue = dataQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();
    }

    @Override
    public void run() {

        String[] message = new String[0];
        byte[] buffer = new byte[200000];
        int validData = 0;
        String received = "";

        try {
            outputStream.write("POST REGISTER;TYPE CLIENT\n".getBytes());
            validData = inputStream.read(buffer);
            statusQueue.put(received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if(received.contains("CODE 0")){
            GUIConverter.setConnected();
        }


        while(continueProcessing) {

            buffer = new byte[200000];
            validData = 0;
            received = "";

            try {
                message = sendQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (message[0].contains("GET STATUS")) {
                try {
                    outputStream.write(message[0].getBytes());
                    validData = inputStream.read(buffer);
                    statusQueue.put(received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8));
                } catch (IOException | InterruptedException e) {
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


            try {
                validData = inputStream.read(buffer);
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
