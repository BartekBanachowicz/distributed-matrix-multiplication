package put.poznan.guiclient;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class StatusThread implements Runnable {
    private BlockingQueue<String[]> queue;
    private Boolean continueProcessing = true;
    private ConnectionThread commThread;

    StatusThread(BlockingQueue<String[]> xQueue) throws IOException {
        this.queue = xQueue;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET STATUS\n";

        try{
            do{
                Thread.sleep(3000);
                queue.put(message);
            }while(this.continueProcessing);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}
