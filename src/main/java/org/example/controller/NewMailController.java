package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Email;
import org.example.model.User;

import java.time.LocalDateTime;
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

    @FXML
    private Button sendButton;

    private ConnexionServer connexionServer = ConnexionServer.getInstance();

    MailBoxController mailBoxController;

    User currentUser;

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

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        setUserMail(currentUser.getEmail());
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

    public void setMailBoxController(MailBoxController mailBoxController) {
        this.mailBoxController = mailBoxController;
    }

    @FXML
    private void initialize() {
        sendButton.setOnMouseEntered(event -> sendButton.setStyle("-fx-cursor: hand;"));
        sendButton.setOnMouseExited(event -> sendButton.setStyle(""));
    }

    @FXML
    private void sendMail() {
        // Validate fields
        if (!areFieldsValid()) {
            return;
        }

        // Vérifier l'existence des destinataires
        String[] recipients = sendTo.getText().split("\\s*;\\s*");
        for (String recipient : recipients) {
            boolean userExists = connexionServer.checkUserExists(currentUser, recipient);
            if (!userExists) {
                // Afficher une alerte
                showAlert("User Not Found", "The user " + recipient + " does not exist on the server.");
                return; // Ne pas continuer l'envoi
            }
        }

        // Créer l'objet Email
        Email email = new Email(
                0,
                mailName.getText(),
                Arrays.asList(recipients),
                subject.getText(),
                content.getText(),
                LocalDateTime.now()
        );

        // Envoyer l'email via ConnexionServer
        boolean success = connexionServer.sendEmail(currentUser, email);

        if (success) {
            // Fermer la fenêtre
            Stage stageNewMail = (Stage) sendTo.getScene().getWindow();
            stageNewMail.close();
        } else {
            showAlert("Error", "Failed to send mail. Please try again.");
        }
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
