package org.example.mail_client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.regex.Pattern;

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

    @FXML
    private void sendMail() {
        // Validate fields
        if (!areFieldsValid()) {
            return;
        }

        // Simulate sending mail
        System.out.println("Mail sent to: " + sendTo.getText());
        System.out.println("Subject: " + subject.getText());
        System.out.println("Content: " + content.getText());

        // Close the current stage
        Stage stageNewMail = (Stage) sendTo.getScene().getWindow();
        stageNewMail.close();
    }

    private boolean areFieldsValid() {
        if (sendTo.getText().isEmpty() || subject.getText().isEmpty() || content.getText().isEmpty()) {
            showAlert("Validation Error", "All fields must be filled.");
            return false;
        }

        if (!areEmailsValid(sendTo.getText())) {
            showAlert("Validation Error", "One or more email addresses are invalid.");
            return false;
        }

        return true;
    }

    private boolean areEmailsValid(String emails) {
        // Delete unnecessary spaces, divide by “;”, manage multiple spaces between e-mails
        String[] emailArray = emails.split("\\s*;\\s*");
        return Arrays.stream(emailArray)
                .allMatch(this::isValidEmail);
    }



    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
