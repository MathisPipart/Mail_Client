package org.example.mail_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mail_client.controller.LogController;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class MailClientApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage logStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("log-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 344, 360);

        LogController controller = fxmlLoader.getController();
        controller.setLogStage(logStage);

        logStage.setTitle("Mail");
        logStage.setScene(scene);
        logStage.show();

        startClient();
    }

    public void startClient(){
        try {
            String nomeLocale = InetAddress.getLocalHost().getHostName();
            System.out.println(nomeLocale);
            Socket s = new Socket(nomeLocale, 8189);

            System.out.println("Ho aperto il socket verso il server.\n");

            try {
                InputStream inStream = s.getInputStream();
                Scanner in = new Scanner(inStream);

                ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());

                System.out.println("Sto per ricevere dati dal socket server!");

                String line = in.nextLine();
                System.out.println(line);

                boolean done = false;
                Vector<Date> leDate = new Vector<Date>();
                for (int i=0; i<4; i++)
                    leDate.add(new Date());

                outStream.writeObject(leDate);

                while (in.hasNextLine()) {
                    line = in.nextLine();
                    System.out.println("Ritorno: " + line);
                }
            }
            finally {
                s.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}