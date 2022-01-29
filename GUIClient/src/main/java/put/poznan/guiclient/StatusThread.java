package put.poznan.guiclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class StatusThread implements Runnable {
    private final BlockingQueue<String[]> sendQueue;
    private final BlockingQueue<String[]> statusQueue;
    private Boolean continueProcessing = true;

    StatusThread(BlockingQueue<String[]> sendQueue, BlockingQueue<String[]> statusQueue) {
        this.sendQueue = sendQueue;
        this.statusQueue = statusQueue;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET STATUS";
        String[] receivedMessage;
        String[] compUnits = new String[0];

        try{
            do{
                Thread.sleep(10000);
                sendQueue.put(message);

                receivedMessage = statusQueue.take();

                if(receivedMessage[0].contains("STATUS IDLE")){
                    GUIClient.getAdapter().setConnected();
                }
                else if(receivedMessage[0].contains("STATUS READY")){
                    GUIClient.getAdapter().setReady();
                }
                else if(receivedMessage[0].contains("STATUS RUNNING")){
                    GUIClient.getAdapter().setRunning();
                }
                else if(receivedMessage[0].contains("STATUS STOPPED")){
                    GUIClient.getAdapter().setStopped();
                }
                else if(receivedMessage[0].contains("STOP")){
                    continueProcessing = false;
                }

                if(receivedMessage[1].length()>0){
                    String[] units = receivedMessage[1].split(";");

                    if(!Arrays.equals(compUnits, units)){
                        compUnits = units;
                        GUIClient.getAdapter().setListOfUnits(compUnits);
                    }
                }
            } while(this.continueProcessing);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}
