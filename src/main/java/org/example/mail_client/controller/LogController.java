package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.mail_client.MailApplication;
import org.example.mail_client.model.User;

import java.util.regex.Pattern;

public class LogController {
    private Stage logStage;

    @FXML
    private TextField mailTextField;
    @FXML
    private Label invalidMailLabel;

    @FXML
    public void openMailBoxStage() throws Exception {
        String mail = mailTextField.getText();

        // mail syntax verification
        if (!isValidEmail(mail)) {
            invalidMailLabel.setVisible(true);
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MailApplication.class.getResource("mailBox-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);

        MailBoxController mailBoxController = fxmlLoader.getController();
        //Pass the login mail to the mail box
        User currentUser = new User(mail);
        mailBoxController.setUser(currentUser);

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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
}
