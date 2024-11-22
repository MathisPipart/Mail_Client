package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.mail_client.model.User;

public class NewMailController {
    @FXML
    private Label mailName;

    @FXML
    private TextField sendTo;

    @FXML
    private TextField subject;

    @FXML
    private TextArea content;

    public Label getMailName() {
        return mailName;
    }

    public TextField getSendTo() {
        return sendTo;
    }

    public TextField getSubject() {
        return subject;
    }

    public TextArea getContent() {
        return content;
    }

    public void setUserMail(String email) {
        mailName.setText(email);
    }

    public void setSendTo(String receiver) {
        sendTo.setText(receiver);
    }

    public void setSubject(String subject) {
        this.subject.setText(subject);
    }

    public void setContent(String content) {
        this.content.setText(content);
    }
}
