package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.mail_client.model.User;

public class NewMailController {
    @FXML
    private Label mailName;

    public void setUserMail(User user) {
        mailName.setText(user.getEmail());
    }
}
