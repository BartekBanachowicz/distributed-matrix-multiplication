package put.poznan.guiclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Math.sqrt;

public class DataHandler {
    private double[][] leftMatrix;
    private double[][] rightMatrix;
    private double[][] resultMatrix;
    private int matrixSize;

    public double[][] loadMatrixFromFile(Path filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(String.valueOf(filePath)));
        scanner.useDelimiter(",");
        ArrayList<Double> buffer = new ArrayList<>();
        double[][] matrix;

        while(scanner.hasNextDouble()){
            buffer.add(scanner.nextDouble());
        }

        if(sqrt(buffer.size())%1 == 0){
            matrixSize = (int) sqrt(buffer.size());
        } else{
            //TODO - There is not square matrix at input.
            System.out.println("It is not a square matrix.");
        }

        matrix = new double[matrixSize][matrixSize];

        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize; j++){
                matrix[i][j] = buffer.get(0);
                buffer.remove(0);
            }
        }
        scanner.close();
        return matrix;
    }

    public void addLeftMatrix(Path filePath) throws FileNotFoundException {
        this.leftMatrix = loadMatrixFromFile(filePath);
    }

    public void addRightMatrix(Path filePath) throws FileNotFoundException {
        this.rightMatrix = loadMatrixFromFile(filePath);
    }

    public double[][] getLeftMatrix() {
        return leftMatrix;
    }

    public double[][] getRightMatrix() {
        return rightMatrix;
    }

    public void generateMatrixToFile(int size, Path filePath){

    }

    private void initializeResultMatrix(){
        this.resultMatrix = new double[this.matrixSize][this.matrixSize];
    }

    public void writeToResultMatrix(int x, int y, double value){
        this.resultMatrix[x][y] = value;
    }

    public void saveMatrixToFile(Path filePath, double[][] matrix) throws FileNotFoundException {
        int matrixSize = matrix.length;
        File outputFile = new File(String.valueOf(filePath));
        PrintWriter writer = new PrintWriter(outputFile);

        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize-1; j++){
                writer.print(matrix[i][j]);
                writer.print(", ");
            }

            writer.print(matrix[i][matrixSize-1]);

            if(i != matrixSize-1){
                writer.print("\n");
            }
        }

        writer.close();
    }

}
