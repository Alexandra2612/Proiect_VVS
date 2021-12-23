package server;

import java.lang.String;

import gui.Gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static gui.Gui.*;
import static server.SocketInit.*;
import static server.SocketInit.port;

public class ServerListener extends Thread {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListener.class);
    public static ServerSocket serverSocket;
    public ServerListener() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }
    public ServerListener(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {

            while ( serverSocket.isBound() && !serverSocket.isClosed()) {//if serverSocket is bound to an address and is not closed
                Socket socket = serverSocket.accept();

                LOGGER.info(" * Connection accepted: " + socket.getInetAddress());
                PrintWriter out = new PrintWriter(socket.getOutputStream(),
                        true,StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(),"UTF-8"));

                String inputLine;
                inputLine = in.readLine();
                if(inputLine != null){
                    System.out.println("Server: " + inputLine);
                    System.out.println(getPath(inputLine));
                    sendFileContent(socket,getPath(inputLine));//tratam exceptie cu sendfailContent --> bad request
                }
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Server: " + inputLine);
                    if (inputLine.trim().equals(""))
                        break;
                }
                while (socket.getKeepAlive()) {

                }

                out.close();
                in.close();
            }

        } catch (IOException e) {
            LOGGER.error("Problem with setting socket", e);
        } finally {
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.error("The connection could not be closed", e);
                }
            }
        }

    }
    public String getPath(String inputLine){
        String path ;
        if(inputLine.substring(0, 3).equals("GET")) {
            path = inputLine.substring(inputLine.indexOf(' ') + 1);
            path = path.substring(0, path.indexOf(' '));
            return path;
        }
        else
            return null;//aruncam o exceptie

    }


    public void sendContent(Socket socket, String html)
    {
        try {
            OutputStream outputStream = socket.getOutputStream();

            final String CRLF = "\r\n"; // string used in http protocol to terminate a line

            String response =
                    "HTTP/1.1 200 OK" + CRLF + // Status Line  :   HTTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                            "Content-Length: " + html.getBytes(StandardCharsets.UTF_8).length + CRLF + // HEADER
                            CRLF +
                            html +
                            CRLF + CRLF;

            outputStream.write(response.getBytes(StandardCharsets.UTF_8));

            LOGGER.info(" * Connection Processing Finished.");
        } catch (IOException e) {
            LOGGER.error("Problem with communication", e);
        }
    }
    public void sendFailContent(Socket socket, String filename)
    {
        try {
            OutputStream outputStream = socket.getOutputStream();

            final String CRLF = "\r\n"; // string used in http protocol to terminate a line
            String type="text/plain";
            if (filename.endsWith(".html") || filename.endsWith(".htm"))
                type="text/html";
            else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
                type="image/jpeg";
            else if (filename.endsWith(".css"))
                type="style/css";

            String response =
                    "HTTP/1.0 404 Not Found"+CRLF+
                            "Content-type: "+type+CRLF+CRLF+
                            "File "+filename+" not found!\n";

            outputStream.write(response.getBytes(StandardCharsets.UTF_8));

            LOGGER.info(" * Connection Processing Finished.");
        } catch (IOException e) {
            LOGGER.error("Problem with communication", e);
        }
    }
    public void sendForbiddenContent(Socket socket, String filename)
    {
        try {
            OutputStream outputStream = socket.getOutputStream();

            final String CRLF = "\r\n"; // string used in http protocol to terminate a line

            String response =
                    "HTTP/1.0 403 Forbidden"+CRLF+
                            "Could not read from "+filename+"\n";

            outputStream.write(response.getBytes(StandardCharsets.UTF_8));

            LOGGER.info(" * Connection Processing Finished.");
        } catch (IOException e) {
            LOGGER.error("Problem with communication", e);
        }
    }
    public void sendBadRequestContent(Socket socket,String filename){
        try {
            OutputStream outputStream = socket.getOutputStream();

            final String CRLF = "\r\n"; // string used in http protocol to terminate a line

            String response =
                    "HTTP/1.0 400 Bad request"+CRLF+
                            "File "+filename+" has the wrong extension!\n";

            outputStream.write(response.getBytes(StandardCharsets.UTF_8));

            LOGGER.info(" * Connection Processing Finished.");
        } catch (IOException e) {
            LOGGER.error("Problem with communication", e);
        }
    }
    public static File findFileInDirectory(String filename) throws IOException {
        String path1 = filename.replace("/","\\");
        String path2 = path1.replaceAll("%20"," ");
        List<Path> files = Files.walk(Paths.get(maintenance ? maintenanceDir:rootDir))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(path2))
                .collect(Collectors.toList());

        files.forEach(System.out::println);

        if (files.size() == 0) {
            return null;
        } else {
            return new File(String.valueOf(files.get(0)));
        }
    }

    public void sendFileContent(Socket socket,String filename)
    {
        //daca fisierul nu e .html poti trimite un raspuns de tip Bad Request
        //filename is '/' or ''?=>vezi daca exista index.html,index.htm etc. si daca exista filename = ala care exista daca nu exista => not found!!
        if(!(filename.endsWith(".html"))&&!(filename.endsWith(".htm"))&&!(filename.endsWith(".css"))){
            if(filename.endsWith("/"))
                sendFileContent(socket,"index.html");
            else {
                sendBadRequestContent(socket, filename);
                return;
            }
        }
        System.out.println(filename);

        StringBuilder continutFisier = new StringBuilder();
        //citire de fisier
        try {

                 BufferedReader in = new BufferedReader(new FileReader(maintenance ? (maintenanceDir + "/index.html"):(rootDir +"/ "+filename),StandardCharsets.UTF_8));
                 String str;
                 while ((str = in.readLine()) != null) {

                     continutFisier.append(str);
                 }
                 in.close();
                 sendContent(socket,continutFisier.toString());

        } catch (IOException e) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(findFileInDirectory(filename),StandardCharsets.UTF_8));
                String str;
                while ((str = in.readLine()) != null) {
                    continutFisier.append(str);
                }
                in.close();
                sendContent(socket,continutFisier.toString());
            } catch (IOException ex){
                sendForbiddenContent(socket,filename);
                LOGGER.error("Failed to read from file", ex);//403 forbidden
            } catch (NullPointerException ex ) {
                sendFailContent(socket,filename);
                LOGGER.error("File not found", ex);//filename leads to a path that does not exist => 404 not found sendFailContent(socket,filename)
            }
        }

    }
}
