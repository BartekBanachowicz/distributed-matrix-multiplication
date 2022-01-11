package put.poznan.guiclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class StatusThread implements Runnable {
    private BlockingQueue<String[]> blockingQueue;
    private Boolean continueProcessing = true;

    StatusThread(BlockingQueue<String[]> xBlockingQueue) throws IOException {
        this.blockingQueue = xBlockingQueue;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET STATUS";

        while(this.continueProcessing){
            try {
                wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.blockingQueue.add(message);
        }

    }
}
