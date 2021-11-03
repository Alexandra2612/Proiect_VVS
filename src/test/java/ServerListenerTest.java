import com.sun.security.ntlm.Server;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class ServerListenerTest {
    private ServerListener sl;

    @BeforeAll
    public void beforeClass() {
        try {
            sl = new ServerListener(10008);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void findFileInDirectoryTrue(){
        try {
            assertEquals(sl.findFileInDirectory("/c.html"),"test_server\\aaa\\bbb\\c.html");
        } catch (IOException e) {
            fail("The file was not found");
        }
    }

    @Test
    void findFileInDirectoryFalse(){
        try {
            assertEquals(sl.findFileInDirectory("/zzzz.html"),null);
        } catch (IOException e) {
            fail("Something went wrong when searching for file");
        }
    }
}
