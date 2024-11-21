package org.example.mail_client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.mail_client.model.Email;

import java.time.LocalDateTime;

public class MailBoxController {

    @FXML
    private Label mailName;
    @FXML
    private Label welcomeText;

    @FXML
    private TableView<Email> emailTable;

    @FXML
    private TableColumn<Email, String> emailColumn;

    @FXML
    private Label selectedSenderLabel;
    @FXML
    private Label selectedReceiverLabel;
    @FXML
    private Label selectedSubjectLabel;
    @FXML
    private Label selectedContentLabel;
    @FXML
    private Label selectedDateLabel;

    @FXML
    private Button replyButton, replyAllButton, forwardButton, deleteButton;

    private final ObservableList<Email> emailList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        listenerOnClickListMail();
        cellsInitialisation();
        addMail();

        emailTable.setItems(emailList);
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void setReceivedMailText(String text) {
        mailName.setText(text);
    }

    private void listenerOnClickListMail(){
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Mettre à jour le label avec le sender de l'email sélectionné
            selectedSenderLabel.setText(newSelection.getSender());
            selectedReceiverLabel.setText(newSelection.getReceiver());
            selectedSubjectLabel.setText(newSelection.getSubject());
            selectedContentLabel.setText(newSelection.getContent());
            selectedDateLabel.setText(newSelection.getTimestamp().toString());
        });
    }

    private void cellsInitialisation(){
        emailTable.setRowFactory(tv -> new TableRow<Email>() {
            @Override
            protected void updateItem(Email item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-background-color: white;"); // Lignes vides : fond blanc
                    setText(null); // Pas de texte
                } else {
                    setStyle(""); // Réinitialiser le style pour les lignes non vides
                }
            }
        });

        // Configurer le CellFactory pour afficher les informations
        emailColumn.setCellFactory(column -> new TableCell<Email, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); // Pas de contenu pour les cellules vides
                    setText(null);
                } else {
                    // Récupérer l'email correspondant à la ligne
                    Email email = (Email) getTableRow().getItem();

                    // Label pour "From"
                    Label fromLabel = new Label("From: " + email.getSender());
                    fromLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    // Label pour "Subject"
                    Label subjectLabel = new Label("Subject: " + email.getSubject());
                    subjectLabel.setStyle("-fx-font-weight: bold;");

                    // Label pour "Content"
                    Label contentLabel = new Label(email.getContent());
                    contentLabel.setStyle("-fx-text-fill: grey;");

                    // Organiser les labels dans un VBox
                    VBox vbox = new VBox(fromLabel, subjectLabel, contentLabel);
                    vbox.setSpacing(5); // Espacement entre les éléments

                    setGraphic(vbox); // Afficher le VBox comme contenu de la cellule
                    setText(null); // Ne pas utiliser de texte brut
                }
            }
        });

        //No Horizontal scrollbar
        emailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void addMail(){
        emailList.addAll(
                new Email("1", "mathis.pipart@gmail.com", "example1@mail.com", "Sujet 1", "Contenu 1", LocalDateTime.now()),
                new Email("2", "mathis.pipart@free.fr", "example2@mail.com", "Sujet 2", "Contenu 2", LocalDateTime.now()),
                new Email("3", "paul.zerial@edu.esiee.fr", "example3@mail.com", "Sujet 3", "Contenu 3", LocalDateTime.now()),
                new Email("4", "alice.durand@gmail.com", "example4@mail.com", "Sujet 4", "Contenu 4", LocalDateTime.now()),
                new Email("5", "julien.martin@orange.fr", "example5@mail.com", "Sujet 5", "Contenu 5", LocalDateTime.now()),
                new Email("6", "emma.lefevre@hotmail.com", "example6@mail.com", "Sujet 6", "Contenu 6", LocalDateTime.now()),
                new Email("7", "lucas.bernard@edu.univ.fr", "example7@mail.com", "Sujet 7", "Contenu 7", LocalDateTime.now()),
                new Email("8", "charlotte.dubois@gmail.com", "example8@mail.com", "Sujet 8", "Contenu 8", LocalDateTime.now()),
                new Email("9", "nicolas.perrin@yahoo.fr", "example9@mail.com", "Sujet 9", "Contenu 9", LocalDateTime.now()),
                new Email("10", "lea.moreau@laposte.net", "example10@mail.com", "Sujet 10", "Contenu 10", LocalDateTime.now()),
                new Email("11", "marie.dupont@gmail.com", "example11@mail.com", "Sujet 11", "Contenu 11", LocalDateTime.now()),
                new Email("12", "quentin.leroy@hotmail.fr", "example12@mail.com", "Sujet 12", "Contenu 12", LocalDateTime.now()),
                new Email("13", "sophie.giraud@edu.univ.fr", "example13@mail.com", "Sujet 13", "Contenu 13", LocalDateTime.now()),
                new Email("14", "antoine.roche@orange.fr", "example14@mail.com", "Sujet 14", "Contenu 14", LocalDateTime.now()),
                new Email("15", "claire.benoit@yahoo.com", "example15@mail.com", "Sujet 15", "Contenu 15", LocalDateTime.now())
        );
    }
}