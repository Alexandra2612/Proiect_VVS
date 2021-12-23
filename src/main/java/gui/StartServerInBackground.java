package gui;
import javax.swing.*;
import java.io.IOException;
import server.ServerListener;

public class StartServerInBackground extends SwingWorker<ServerListener,Integer> {


    public ServerListener getConn() {
        return server;
    }

    public ServerListener server;

    {
        try {
            server = new ServerListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ServerListener doInBackground() throws Exception {
        server.start();
        return null;
    }

    @Override
    public void done() {

    }
}
