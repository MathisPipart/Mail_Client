package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.ConnexionServer;
import org.example.controller.LogController;

import java.io.IOException;

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

        // Exécuter la connexion au serveur dans un thread séparé
        Thread connectionThread = new Thread(() -> {
            ConnexionServer connexionServer = ConnexionServer.getInstance();
            connexionServer.startClient();
        });

        connectionThread.setDaemon(true); // Le thread s'arrête automatiquement lorsque l'application se ferme
        connectionThread.start();
    }


}