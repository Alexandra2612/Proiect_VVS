import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ServerListenerTest {
    private static ServerListener sl;

    @BeforeClass
    public static void beforeClass() {
        try {
            sl = new ServerListener(10008);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void findFileInDirectoryTrue(){
        try {
            assertEquals(sl.findFileInDirectory("/c.html").getPath(), "test_server\\aaa\\bbb\\c.html");
        } catch (IOException e) {
            fail("The file was not found");
        }
    }

    @Test
    public void findFileInDirectoryFalse() throws IOException {
            assertEquals(sl.findFileInDirectory("/zzzzzzzzzzzzzzzz.html"),null);

    }

    @Test
    public void testSendFileContent_BadRqeust() throws IOException {

        class ServerListenerMock extends ServerListener{
            public int success=0;

            public ServerListenerMock(int port) throws IOException {
                super(port);
            }

            @Override
            public void sendBadRequestContent(Socket socket, String filename) {
                success=1;
            }
        }
        ServerListenerMock serverListenerMock = new ServerListenerMock(10009) ;

        serverListenerMock.sendFileContent(null, "woaaah");
        assertEquals(1,serverListenerMock.success);

    }

    //TODO test sendFileContent success scenario (create a file with some data, then call method on file and mock sendContent() method).


    //TODO test sendContent methods by giving them a socket where you control both ends, calling
    // the method and then reading from the other end of the socket


}
