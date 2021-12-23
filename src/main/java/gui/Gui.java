package gui;
import server.ServerListener;
import server.SocketInit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Gui extends JFrame {
    private JPanel FirstPanel;
    private JButton startServerButton;
    private JButton stopServerButton;
    private JCheckBox switchToMaintenanceModeCheckBox;
    private JTextField textField3;
    private JTextField textField1;
    private JTextField textField2;
    private JLabel LabelFirst;
    private JLabel LabelSecond;
    private JLabel LabelThird;
    private JLabel ValidateRoot;
    private int port = 10051;
    public static boolean maintenance = false;
    public static String rootDir = "test_server";
    public static String maintenanceDir = "maintenance";
    private String lastsetRoot=rootDir;
    StartServerInBackground swingWorker;

    public Gui(String text) {
        super(text);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(FirstPanel);
        this.pack();
        this.LabelFirst.setText("stopped");
        this.LabelSecond.setText("stopped");
        this.LabelThird.setText("stopped");
        this.textField1.setText(String.valueOf(port));
        this.textField3.setText(String.valueOf(rootDir));
        this.textField2.setText(String.valueOf(maintenanceDir));
        this.stopServerButton.setEnabled(false);
        this.switchToMaintenanceModeCheckBox.setEnabled(false);


        switchToMaintenanceModeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
               maintenance = !maintenance;
            }
        });
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LabelFirst.setText("running");
                LabelThird.setText(String.valueOf(SocketInit.port));
                LabelSecond.setText("127.0.0.1");
                System.out.println("PORT:" + SocketInit.port);

                swingWorker = new StartServerInBackground();
                try {
                    swingWorker.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startServerButton.setEnabled(false);
                stopServerButton.setEnabled(true);
                switchToMaintenanceModeCheckBox.setSelected(false);
                switchToMaintenanceModeCheckBox.setEnabled(true);
                textField1.setEnabled(false);
                textField2.setEnabled(true);
                textField3.setEnabled(false);

            }
        });

        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LabelFirst.setText("not running");
                LabelThird.setText("not running");
                LabelSecond.setText("not running");
                swingWorker.server.stop();
                try {
                    ServerListener.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("stopped");
                stopServerButton.setEnabled(false);
                startServerButton.setEnabled(true);
                textField1.setEnabled(true);
                textField3.setEnabled(true);
                textField2.setEnabled(false);
                switchToMaintenanceModeCheckBox.setSelected(false);
                switchToMaintenanceModeCheckBox.setEnabled(false);
                System.out.println("PORT:" + port);

            }
        });

        textField1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String text = textField1.getText();
                System.out.println(text);
                try {
                    SocketInit.port = Integer.parseInt(text);
                } catch (NumberFormatException numberFormatException) {
                    System.out.println("This is not a number");
                }
            }
        });
        textField3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String text = textField3.getText();
                System.out.println(text);
                rootDir = text;
                try {
                    if (validateRootDirectory(text)) {
                        ValidateRoot.setText("The root directory exists");
                        startServerButton.setEnabled(true);
                        lastsetRoot = text;
                    } else {
                        ValidateRoot.setText("The root directory cannot be found");
                        rootDir = lastsetRoot;
                        startServerButton.setEnabled(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

    }
    private boolean validateRootDirectory(String text) throws IOException {
        rootDir = text;
        String path1 = rootDir.replace("/","\\");
        File file = new File(path1);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        return false;
    }
    public static void main(String[] args){
        JFrame jframe = new Gui("Started server");
        jframe.setVisible(true);
    }

}
