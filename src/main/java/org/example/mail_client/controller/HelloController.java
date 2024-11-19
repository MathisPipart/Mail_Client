package org.example.mail_client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mail_client.model.Email;

import java.time.LocalDateTime;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private TableView<Email> emailTable;

    @FXML
    private TableColumn<Email, String> emailColumn;

    @FXML
    private Button replyButton, replyAllButton, forwardButton, deleteButton;

    private final ObservableList<Email> emailList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialisation des colonnes de la table
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubject()));

        // Chargement initial des emails
        emailList.addAll(
                new Email("1", "mathis.pipart@gmail.com", "example1@mail.com", "Sujet 1", "Contenu 1", LocalDateTime.now()),
                new Email("2", "mathis.pipart@gmail.com", "example2@mail.com", "Sujet 2", "Contenu 2", LocalDateTime.now())
        );

        emailTable.setItems(emailList);
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

}