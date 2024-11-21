package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.mail_client.MailApplication;

public class LogController {
    private Stage logStage;

    @FXML
    private TextField mailTextField;

    @FXML
    public void openMailBoxStage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MailApplication.class.getResource("mailBox-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);

        MailBoxController mailBoxController = fxmlLoader.getController();
        //Pass the login mail to the mail box
        String mail = mailTextField.getText();
        mailBoxController.setReceivedMailText(mail);

        Stage stage = new Stage();
        stage.setTitle("Mail");
        stage.setScene(scene);
        stage.show();

        if (logStage != null) {
            logStage.close();
        }
    }

    public void setLogStage(Stage stage) {
        this.logStage = stage;
    }
}
