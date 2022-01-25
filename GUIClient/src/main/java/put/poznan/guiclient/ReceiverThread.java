package put.poznan.guiclient;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread implements Runnable {
    private BlockingQueue<String[]> sendQueue;
    private BlockingQueue<String> dataQueue;
    private Boolean continueProcessing = true;
    private ConnectionThread commThread;
    private DataHandler dataHandler;

    ReceiverThread(BlockingQueue<String[]> sendQueue, BlockingQueue<String> dataQueue, DataHandler dataHandler) throws IOException {
        this.sendQueue = sendQueue;
        this.dataQueue = dataQueue;
        this.dataHandler = dataHandler;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET UPDATE-NEW\n";
        Scanner scanner;
        String[] receivedMessage;
        double resultMatrixValuesCounter = 0;

        if(dataHandler.getMatrixSize() != -1){
            dataHandler.createResultMatrix();
        }

        try{
            do{
                Thread.sleep(10000);
                sendQueue.put(message);

                scanner = new Scanner(dataQueue.take());
                scanner.useDelimiter(";");
                String line;
                String[] values;
                int x, y;
                double val;

                if(scanner.hasNextLine()){
                    line = scanner.nextLine();

                    if(line.matches("RESULTS \\d+") && scanner.hasNextLine()){
                        values = scanner.nextLine().split(";");

                        for (String value : values) {
                            x = Integer.parseInt(value.split(" ")[0]);
                            y = Integer.parseInt(value.split(" ")[1]);
                            val = Double.parseDouble(value.split(" ")[2]);

                            dataHandler.writeToResultMatrix(x, y, val);
                            resultMatrixValuesCounter++;
                        }

                        GUIClient.getAdapter().changeProgress(resultMatrixValuesCounter/(dataHandler.getResultMatrixSize()*dataHandler.getResultMatrixSize()));
                    }
                }
            }while(this.continueProcessing);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

    }
}
