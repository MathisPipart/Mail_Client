package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.MailClientApplication;
import org.example.model.User;

import java.util.regex.Pattern;
public class LogController {
    private Stage logStage;

    @FXML
    private TextField mailTextField;
    @FXML
    private Label invalidMailLabel;

    @FXML
    public void openMailBoxStage() throws Exception {
        String mailName = mailTextField.getText();

        // mail syntax verification
        if (!isValidEmail(mailName)) {
            invalidMailLabel.setVisible(true);
            return;
        }

        User currentUser = new User(mailName);
        ConnexionServer connexionServer = ConnexionServer.getInstance();

        // Tentative de connexion au serveur (en mode synchrone ici)
        boolean connected = connexionServer.startClient(currentUser);

        if (!connected) {
            // L'utilisateur n'est pas reconnu par le serveur
            showAlertUserNotFound();
            return; // On stoppe ici, on n'ouvre pas la MailBox
        }

        // Si on est arrivé ici, c'est que le serveur reconnait l'utilisateur

        FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("mailBox-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);
        MailBoxController mailBoxController = fxmlLoader.getController();
        mailBoxController.setUser(currentUser);

        // On associe le mailBoxController au ConnexionServer pour les mails, etc.
        connexionServer.setMailBoxController(mailBoxController);

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

    private void showAlertUserNotFound() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Utilisateur non reconnu");
        alert.setHeaderText(null);
        alert.setContentText("L'utilisateur n'est pas connu du serveur. Veuillez vérifier votre adresse.");
        alert.showAndWait();
    }
}
