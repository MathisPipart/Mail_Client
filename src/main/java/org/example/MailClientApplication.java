package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    }
}