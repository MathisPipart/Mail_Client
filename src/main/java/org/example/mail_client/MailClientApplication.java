package org.example.mail_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mail_client.controller.ConnexionServer;
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

        ConnexionServer connexionServer = new ConnexionServer();
        connexionServer.startClient();
    }


}