package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SocketInit {
    private final static Logger LOGGER = LoggerFactory.getLogger(SocketInit.class);
    public static int port = 10051;
    public static void main(String[] args) {

        LOGGER.info("Server starting...");

        try {
            ServerListener serverListenerThread = new ServerListener();
            serverListenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
