package org.example.controller;

import javafx.application.Platform;
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

        if (!isValidEmail(mailName)) {
            invalidMailLabel.setVisible(true);
            return;
        }

        User currentUser = new User(mailName);
        ConnexionServer connexionServer = ConnexionServer.getInstance();
        boolean connected = connexionServer.startClient(currentUser);

        if (!connected) {
            showAlertUserNotFound();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("mailBox-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);
        MailBoxController mailBoxController = fxmlLoader.getController();
        mailBoxController.setUser(currentUser);

        connexionServer.setMailBoxController(mailBoxController);

        Stage stage = new Stage();
        stage.setTitle("Mail");
        stage.setScene(scene);
        stage.show();

        if (logStage != null) {
            logStage.close();
        }

        stage.setOnCloseRequest(event -> {
            mailBoxController.stopUpdating();
            connexionServer.closeClientConnection();
            Platform.exit();
        });
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
        alert.setTitle("Unrecognized user");
        alert.setHeaderText(null);
        alert.setContentText("The user is not known to the server. Please check your address.");
        alert.showAndWait();
    }

}
