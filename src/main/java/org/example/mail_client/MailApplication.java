package org.example.mail_client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mail_client.controller.LogController;

import java.io.IOException;

public class MailApplication extends Application {
    @Override
    public void start(Stage logStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MailApplication.class.getResource("log-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 344, 360);

        LogController controller = fxmlLoader.getController();
        controller.setLogStage(logStage);

        logStage.setTitle("Mail");
        logStage.setScene(scene);
        logStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}