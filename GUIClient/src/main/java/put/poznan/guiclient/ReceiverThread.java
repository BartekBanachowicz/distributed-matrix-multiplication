package put.poznan.guiclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread implements Runnable {
    private BlockingQueue<String[]> sendQueue;
    private BlockingQueue<String> dataQueue;
    private Boolean continueProcessing = true;
    private ConnectionThread commThread;
    private final DataHandler dataHandler;
    private boolean finished = false;

    ReceiverThread(BlockingQueue<String[]> sendQueue, BlockingQueue<String> dataQueue, DataHandler dataHandler) throws IOException {
        this.sendQueue = sendQueue;
        this.dataQueue = dataQueue;
        this.dataHandler = dataHandler;
    }

    @Override
    public void run() {
        String[] message = new String[1];
        message[0] = "GET UPDATE-NEW";
        Scanner scanner;

        if(dataHandler.getResultMatrixSize() != -1){
            System.out.println(dataHandler.getResultMatrixSize());
            dataHandler.createResultMatrix();
        }

        int resultMatrixSize = dataHandler.getResultMatrixSize();
        int numberOfResultValues = dataHandler.getResultMatrixSize()*dataHandler.getResultMatrixSize();
        double resultMatrixValuesCounter = 0;
        boolean[][] controlMatrix = new boolean[resultMatrixSize][resultMatrixSize];

        for(int i=0; i<resultMatrixSize; i++){
            for(int j=0; j<resultMatrixSize; j++){
                controlMatrix[i][j] = false;
            }
        }

        try{
            do{
                Thread.sleep(10000);
                sendQueue.put(message);

                scanner = new Scanner(dataQueue.take());
                String line;
                String[] values;
                int x, y;
                double val;

                if(scanner.hasNextLine()){
                    line = scanner.nextLine();

                    if(line.contains("FINISHED")){
                        finished = false;
                    }
                    else if(line.contains("RESULTS") && scanner.hasNextLine()){
                        values = scanner.nextLine().split(";");
                        System.out.println("I");
                        for (String value : values) {
                            x = Integer.parseInt(value.split(" ")[0]);
                            y = Integer.parseInt(value.split(" ")[1]);
                            val = Double.parseDouble(value.split(" ")[2]);

                            if(!controlMatrix[x][y]){
                                dataHandler.writeToResultMatrix(x, y, val);
                                controlMatrix[x][y] = true;
                                resultMatrixValuesCounter += 1.0;
                            }
                        }

                        GUIClient.getAdapter().changeProgress(resultMatrixValuesCounter/(numberOfResultValues));
                    }
                }

                System.out.println("Odebrane: "+resultMatrixValuesCounter);

                if(finished && resultMatrixValuesCounter == numberOfResultValues){
                    continueProcessing = false;
                    GUIClient.getAdapter().changeProgress(0.0);
                    dataHandler.saveResultMatrixToFile();
                    message[0] = "PUT PROCESS-RESET";
                    sendQueue.put(message);
                }

            }while(this.continueProcessing);



        } catch(InterruptedException | FileNotFoundException e){
            e.printStackTrace();
        }

    }
}
