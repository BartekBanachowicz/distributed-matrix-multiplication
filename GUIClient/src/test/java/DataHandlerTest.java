import org.junit.jupiter.api.Test;
import put.poznan.guiclient.DataHandler;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class DataHandlerTest {

    @Test
    void readMatrixFromFile() throws FileNotFoundException {
        DataHandler handler = new DataHandler();
        handler.setLeftMatrixPath(Path.of("C:\\Users\\Bartek Banachowicz\\Desktop\\temp1.csv"));
        handler.addLeftMatrix();

        System.out.println(handler.getLeftMatrixAsString());
    }

}
