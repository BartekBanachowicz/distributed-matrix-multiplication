package put.poznan.guiclient;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class StatusThread implements Runnable {
    private BlockingQueue<String[]> sendQueue;
    private BlockingQueue<String> statusQueue;
    private Boolean continueProcessing = true;
    private ConnectionThread commThread;

    StatusThread(BlockingQueue<String[]> sendQueue, BlockingQueue<String> statusQueue) throws IOException {
        this.sendQueue = sendQueue;
        this.statusQueue = statusQueue;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET STATUS\n";
        Scanner scanner;
        String[] receivedMessage;

        try{
            do{
                Thread.sleep(10000);
                sendQueue.put(message);

                scanner = new Scanner(statusQueue.take());
                scanner.useDelimiter(";");
                receivedMessage = new String[2];
                String line;

                if(scanner.hasNextLine()){
                    line = scanner.nextLine();

                    if(line.contains("STATUS")){
                        receivedMessage = line.split(";");
                    }
                }

            }while(this.continueProcessing);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}
