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
                String line;

                if(scanner.hasNextLine()){
                    line = scanner.nextLine();

                    if(line.contains("STATUS")){
                        receivedMessage = line.split(";");

                        if(receivedMessage[1].contains("STATUS IDLE")){
                            GUIClient.getAdapter().setConnected();
                        }
                        else if(receivedMessage[1].contains("STATUS READY")){
                            GUIClient.getAdapter().setReady();
                        }
                        else if(receivedMessage[1].contains("STATUS RUNNING")){
                            GUIClient.getAdapter().setRunning();
                        }
                        else if(receivedMessage[1].contains("STATUS STOPPED")){
                            GUIClient.getAdapter().setStopped();
                        }

                        if(receivedMessage.length > 2 && receivedMessage[1].split(" ")[0].matches("UNITS")
                                && Integer.parseInt(receivedMessage[1].split(" ")[1]) > 0){

                            System.out.println("Has units");
                            if(scanner.hasNextLine()){
                                String[] units = scanner.nextLine().split(";");
                                GUIClient.getAdapter().setListOfUnits(units);
                            }
                        }
                    }
                }

            }while(this.continueProcessing);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}
