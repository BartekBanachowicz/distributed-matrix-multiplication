package put.poznan.guiclient;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ConnectionThread implements Runnable{
    private final Socket clientSocket;
    private final BlockingQueue<String[]> sendQueue;
    private final BlockingQueue<String[]> statusQueue;
    private final BlockingQueue<String> dataQueue;
    private Boolean continueProcessing = true;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private boolean finished = false;

    ConnectionThread(Socket xClientSocket, BlockingQueue<String[]> sendQueue, BlockingQueue<String[]> statusQueue,
                     BlockingQueue<String> dataQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.sendQueue = sendQueue;
        this.statusQueue = statusQueue;
        this.dataQueue = dataQueue;
        InputStream inputStream = this.clientSocket.getInputStream();
        OutputStream outputStream = this.clientSocket.getOutputStream();
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.writer = new PrintWriter(outputStream, false);
    }

    private void handleErrors(String message){

    }

    private void makeUpConnection(){
        GUIClient.getAdapter().setConnecting();
        String message = "PUT REGISTER-CLIENT\n";
        String serverMessage = "";
        writer.flush();
        try {
            writer.print(message);
            writer.flush();
            serverMessage = reader.readLine();
            System.out.println(serverMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(serverMessage.contains("CODE 0")){
            GUIClient.getAdapter().setConnected();
        }
    }

    private void getStatus(String[] message){
        String[] receivedStatus = new String[2];

        try {
            writer.print(message[0]+"\n");
            writer.flush();
            System.out.println("GET STATUS");
            String serverMessage = reader.readLine();
            System.out.println(serverMessage);

            if(serverMessage.contains("FINISHED")){
                this.finished = true;
            }

            if(!serverMessage.contains("STATUS")){
                handleErrors(serverMessage);
            } else {
                receivedStatus[0] = serverMessage;
                receivedStatus[1] = "";
                if(serverMessage.matches(".+UNITS \\d+$")
                        && Integer.parseInt(serverMessage.split(";")[2].split(" ")[1])>0){
                    serverMessage = reader.readLine();
                    receivedStatus[1] = serverMessage;
                }
                statusQueue.put(receivedStatus);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postMatrix(String[] message){
        String serverMessage;

        try {
            writer.print(message[0]+"\n");

            for(int i=1; i<message.length; i++){
                writer.print(message[i]);
            }
            writer.print("\n");
            writer.flush();

            serverMessage = reader.readLine();
            System.out.println(serverMessage);

            if(!serverMessage.matches("CODE 0")){
                handleErrors(serverMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUpdateNew(String[] message){
        String serverMessage;

        try {

            if(finished){
                serverMessage = "FINISHED";
                dataQueue.put(serverMessage);
                finished = false;
                return;
            }

            writer.print(message[0]+"\n");
            writer.flush();
            System.out.println("GET UPDATE");

            serverMessage = reader.readLine();

            if(!serverMessage.contains("RESULTS")){
                handleErrors(serverMessage);
            } else {

                if(Integer.parseInt(serverMessage.split(";")[1].split(" ")[1]) > 0){
                    serverMessage += "\n" + reader.readLine();
                }
                System.out.println(serverMessage);
                dataQueue.put(serverMessage);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putReset(String[] message) {
        writer.print(message[0]+"\n");
        writer.flush();

        String serverMessage;
        try {
            serverMessage = reader.readLine();
            if(!serverMessage.matches("CODE 0")){
                handleErrors(serverMessage);
            }
        } catch (IOException e) {
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
                getUpdateNew(message);
            }
            else if (message[0].contains("PUT PROCESS-RESET")) {
                putReset(message);
            }
            else if (message[0].contains("ABORT")){
                message[0] = "PUT PROCESS-RESET";
                putReset(message);
            }
            else if (message[0].contains("STOP")){
                continueProcessing = false;
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
