package put.poznan.guiclient;

import javax.sound.midi.Receiver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread implements Runnable{
    private Socket clientSocket;
    public BlockingQueue<String[]> queue;
    private Boolean continueProcessing = true;
    private InputStream inputStream;
    private OutputStream outputStream;

    ReceiverThread(Socket xClientSocket, BlockingQueue<String[]> xQueue) throws IOException {
        this.clientSocket = xClientSocket;
        this.queue = xQueue;
        this.inputStream = this.clientSocket.getInputStream();
        this.outputStream = this.clientSocket.getOutputStream();
    }

    @Override
    public void run() {

        String message = "";

        while(continueProcessing){

            try {
                //System.out.println("Receiver ready");
                message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Done");
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
