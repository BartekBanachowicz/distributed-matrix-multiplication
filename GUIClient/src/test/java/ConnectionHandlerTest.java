import org.junit.jupiter.api.Test;
import put.poznan.guiclient.ConnectionHandler;

import java.io.IOException;

public class ConnectionHandlerTest {
    @Test
    void testConnectionHandler_establishConnection_expextingSuccess() throws IOException {
        ConnectionHandler handler = new ConnectionHandler();
        handler.setConnectionParams(9090, "172.20.127.248");
        handler.establishConnection();
    }
}
