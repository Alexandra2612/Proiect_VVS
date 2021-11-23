import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerListenerTest {
    private static ServerListener sl;
    private String directory = "test_server";
    private static Socket mockSocket;
    private ByteArrayOutputStream mockOutput;
    @BeforeClass
    public static void beforeClass() {
        mockSocket = new Socket();
        mockSocket = mock(Socket.class);
        try {
            sl = new ServerListener(10008);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Before
    public void socketSetup() throws IOException {
        mockOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(mockOutput);
    }
    @Test
    public void testFindFileInDirectoryTrue(){
        Path path = Paths.get(directory + "\\aaa\\bbb\\m.html");
        try {
            Path p = path.getParent();
            if(p!=null)
               Files.createDirectories(p);
            if(!Files.exists(path))
                Files.createFile(path);
            File f = sl.findFileInDirectory("/m.html");
            assertEquals(f.getPath(), "test_server\\aaa\\bbb\\m.html");
            assertTrue(f.exists());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            fail("The file was not found");
        }
    }

    @Test
    public void testFindFileInDirectoryFalse() throws IOException {
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

    @Test
    public void testSendContent(){
        String content = "Content to send";
        try {

            sl.sendContent(mockSocket, content);
            assertEquals(mockOutput.toString(StandardCharsets.UTF_8),"HTTP/1.1 200 OK\r\n" +
                    "Content-Length: 15\r\n" +
                    "\r\n" +
                    "Content to send\r\n\r\n");
        }catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendForbiddenContent(){
        String content = "a.html";
        try {

            sl.sendForbiddenContent(mockSocket, content);
            assertEquals(mockOutput.toString(StandardCharsets.UTF_8),"HTTP/1.0 403 Forbidden\r\n"+
                    "Could not read from "+content+"\n");
        }catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendBadRequestContent(){
        String content = "a.html";
        try {

            sl.sendBadRequestContent(mockSocket, content);
            assertEquals(mockOutput.toString(StandardCharsets.UTF_8),"HTTP/1.0 400 Bad request"+"\r\n"+
                    "File "+content+" has the wrong extension!\n");
        }catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendFailContent(){
        String filename = "a.html";
        String type = "text/html";
        try {

            sl.sendFailContent(mockSocket, filename);
            assertEquals(mockOutput.toString(StandardCharsets.UTF_8),"HTTP/1.0 404 Not Found"+"\r\n"+
                    "Content-type: "+type+"\r\n\r\n"+
                    "File "+filename+" not found!\n");
        }catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    //TODO test sendFileContent success scenario (create a file with some data, then call method on file and mock sendContent() method).


    //TODO test sendContent methods by giving them a socket where you control both ends, calling
    // the method and then reading from the other end of the socket


}
