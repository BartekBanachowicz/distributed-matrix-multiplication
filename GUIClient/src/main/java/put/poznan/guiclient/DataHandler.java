package put.poznan.guiclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Math.sqrt;

public class DataHandler {
    private double[][] leftMatrix;
    private double[][] rightMatrix;
    private double[][] resultMatrix;
    private Path leftMatrixPath;
    private Path rightMatrixPath;
    private Path resultMatrixPath;
    private Path newMatrixPath;
    private int matrixSize = -1;
    private int resultMatrixSize = -1;
    private int newMatrixSize = -1;

    public double[][] loadMatrixFromFile(Path filePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(String.valueOf(filePath)));
        scanner.useDelimiter(",");
        ArrayList<String> buffer = new ArrayList<>();
        double[][] matrix;
        String line;

        while(scanner.hasNextLine()){
            line = scanner.nextLine();
            buffer.addAll(List.of(line.split(",")));
        }

        if(sqrt(buffer.size())%1 == 0){
            matrixSize = (int) sqrt(buffer.size());
            resultMatrixSize = matrixSize;
        } else{
            //TODO - There is not square matrix at input.
            System.out.println("It is not a square matrix.");
        }

        matrix = new double[matrixSize][matrixSize];

        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize; j++){
                matrix[i][j] = Double.parseDouble(buffer.get(0));
                buffer.remove(0);
            }
        }
        scanner.close();
        return matrix;
    }

    public void addLeftMatrix() throws FileNotFoundException {
        this.leftMatrix = loadMatrixFromFile(this.leftMatrixPath);
    }

    public void addRightMatrix() throws FileNotFoundException {
        this.rightMatrix = loadMatrixFromFile(this.rightMatrixPath);
    }

    public String getLeftMatrixAsString(){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize; j++){
                builder.append(leftMatrix[i][j]);
                if(j!=matrixSize-1){
                    builder.append(" ");
                }
            }
            if(i!=matrixSize-1){
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public String getRightMatrixAsString(){
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize; j++){
                builder.append(rightMatrix[i][j]);
                if(j!=matrixSize-1){
                    builder.append(" ");
                }
            }
            if(i!=matrixSize-1){
                builder.append(";");
            }
        }
        return builder.toString();
    }

    public Path getLeftMatrixPath() {
        return leftMatrixPath;
    }

    public Path getRightMatrixPath() {
        return rightMatrixPath;
    }

    public void generateMatrixToFile() throws FileNotFoundException {
        Random random = new Random();
        double[][] newMatrix = new double[newMatrixSize][newMatrixSize];
        for(int i = 0; i< newMatrixSize; i++){
            for(int j=0; j<newMatrixSize; j++){
                newMatrix[i][j] = random.nextInt(1000);
            }
        }
        saveMatrixToFile(newMatrixPath, newMatrix);
    }

    public void setNewMatrixPath(Path newMatrixPath) {
        this.newMatrixPath = newMatrixPath;
    }

    public void setNewMatrixSize(int newMatrixSize) {
        this.newMatrixSize = newMatrixSize;
    }

    public int getNewMatrixSize() {
        return newMatrixSize;
    }

    public Path getNewMatrixPath() {return newMatrixPath; }

    public void setLeftMatrixPath(Path leftMatrixPath) {
        this.leftMatrixPath = leftMatrixPath;
    }

    public void setRightMatrixPath(Path rightMatrixPath) {
        this.rightMatrixPath = rightMatrixPath;
    }

    public void initializeResultMatrix(){
        this.resultMatrix = new double[this.matrixSize][this.matrixSize];
    }

    public Path getResultMatrixPath() {
        return resultMatrixPath;
    }

    public int getResultMatrixSize(){ return resultMatrixSize; }

    public void setResultMatrixPath(Path resultMatrixPath) {
        this.resultMatrixPath = resultMatrixPath;
    }

    public void writeToResultMatrix(int x, int y, double value){
        this.resultMatrix[x][y] = value;
    }

    public void createResultMatrix(){
        if(this.matrixSize != -1){
            this.resultMatrixSize = this.matrixSize;
            this.resultMatrix = new double[resultMatrixSize][resultMatrixSize];
        }
    }

    public void saveMatrixToFile(Path filePath, double[][] matrix) throws FileNotFoundException {
        int matrixSize = matrix.length;
        File outputFile = new File(String.valueOf(filePath));
        PrintWriter writer = new PrintWriter(outputFile);

        for(int i=0; i<matrixSize; i++){
            for(int j=0; j<matrixSize-1; j++){
                writer.print(matrix[i][j]);
                writer.print(",");
            }

            writer.print(matrix[i][matrixSize-1]);

            if(i != matrixSize-1){
                writer.print("\n");
            }
        }

        writer.close();
    }

    public void saveResultMatrixToFile() throws FileNotFoundException {
        saveMatrixToFile(this.resultMatrixPath, this.resultMatrix);
    }

}
