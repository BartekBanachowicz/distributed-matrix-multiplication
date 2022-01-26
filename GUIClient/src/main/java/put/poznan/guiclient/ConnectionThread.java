package put.poznan.guiclient;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class ConnectionThread implements Runnable{
    private final Socket clientSocket;
    private final BlockingQueue<String[]> sendQueue;
    private final BlockingQueue<String[]> statusQueue;
    private final BlockingQueue<String> dataQueue;
    private Boolean continueProcessing = true;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    ConnectionThread(Socket xClientSocket, BlockingQueue<String[]> sendQueue, BlockingQueue<String[]> statusQueue,
                     BlockingQueue<String> dataQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.sendQueue = sendQueue;
        this.statusQueue = statusQueue;
        this.dataQueue = dataQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();
    }

    private void handleErrors(String message){

    }

    private void makeUpConnection(){
        byte[] buffer = new byte[200000];
        int validData;
        String received = "";

        GUIClient.getAdapter().setConnecting();

        try {
            outputStream.write("PUT REGISTER-CLIENT\n".getBytes());
            validData = inputStream.read(buffer);
            received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(received.contains("CODE 0")){
            GUIClient.getAdapter().setConnected();
        }
    }

    private void getStatus(String[] message){
        byte[] buffer = new byte[200000];
        int validData;
        String[] receivedStatus = new String[2];

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintWriter writer = new PrintWriter(outputStream, true);


        try {

            writer.println(message[0]);
            String serverMessage = reader.readLine();
            System.out.println(serverMessage);

            /*outputStream.write(message[0].getBytes());
            validData = inputStream.read(buffer);
            received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8);
            System.out.println(received);*/

            if(!serverMessage.contains("STATUS")){
                handleErrors(serverMessage);
            } else {
                receivedStatus[0] = serverMessage;
                if(serverMessage.matches(".+UNITS \\d+$")
                        && Integer.parseInt(serverMessage.split(";")[2].split(" ")[1])>0){
                    serverMessage = reader.readLine();
                    System.out.println(serverMessage);
                    receivedStatus[1] = serverMessage;
                }
                statusQueue.put(receivedStatus);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postMatrix(String[] message){
        byte[] buffer = new byte[200000];
        int validData;
        String received;

        try {
            System.out.println(message[0]);
            System.out.println(message[1]);
            outputStream.write(message[0].getBytes());
            outputStream.write(message[1].getBytes());

            validData = inputStream.read(buffer);
            received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8);

            if(!received.matches("CODE 0")){
                handleErrors(received);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUpdate(String[] message){
        byte[] buffer = new byte[200000];
        int validData;
        String received;

        try {
            outputStream.write(message[0].getBytes());
            validData = inputStream.read(buffer);
            received = new String(buffer, 0, validData-1, StandardCharsets.UTF_8);

            if(!received.contains("RESULTS")){
                handleErrors(received);
            } else {
                dataQueue.put(received);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String[] message = new String[0];

        makeUpConnection();

        while(continueProcessing) {

            try {
                message = sendQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (message[0].contains("GET STATUS")) {
                getStatus(message);
            }
            else if (message[0].contains("POST")) {
                postMatrix(message);
            }
            else if (message[0].contains("GET UPDATE")) {
                getUpdate(message);
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
