package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class NewMailController {
    @FXML
    private Label mailName;

    public void setUserMail(String text) {
        mailName.setText(text);
    }
}
