package org.example.mail_client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.mail_client.MailApplication;
import org.example.mail_client.model.Email;
import org.example.mail_client.model.MailBox;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class MailBoxController {

    @FXML
    private Label mailName;

    @FXML
    private TableView<Email> emailTable;

    @FXML
    private TableColumn<Email, String> emailColumn;

    @FXML
    private Label selectedSenderLabel;
    @FXML
    private TextFlow selectedReceiverTextFlow;
    @FXML
    private Label selectedSubjectLabel;
    @FXML
    private TextFlow selectedContentTextFlow;
    @FXML
    private Label selectedDateLabel;

    @FXML
    private Button replyButton, replyAllButton, forwardButton, deleteButton;

    private final MailBox mailBox = new MailBox();

    @FXML
    public void initialize() {
        listenerOnClickListMail();
        cellsInitialisation();
        addMail();

        emailTable.setItems(mailBox.getEmails());
    }

    @FXML
    public void openNewMailStage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MailApplication.class.getResource("newMail-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 598, 488);

        NewMailController controller = fxmlLoader.getController();
        controller.setUserMail(mailName.getText());

        Stage newMailStage = new Stage();
        newMailStage.setTitle("New Mail");
        newMailStage.setScene(scene);
        newMailStage.show();
    }

    public void setReceivedMailText(String text) {
        mailName.setText(text);
    }

    private void listenerOnClickListMail(){
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Mettre à jour le label avec le sender de l'email sélectionné
            selectedSenderLabel.setText(newSelection.getSender());
            updateReceiverTextFlow(newSelection.getReceiver());
            selectedSubjectLabel.setText(newSelection.getSubject());
            selectedDateLabel.setText(newSelection.getTimestamp().toString());

            selectedContentTextFlow.getChildren().clear();
            Text contentText = new Text(newSelection.getContent());
            selectedContentTextFlow.getChildren().add(contentText);
        });
    }

    @FXML
    private void updateReceiverTextFlow(List<String> receivers) {
        selectedReceiverTextFlow.getChildren().clear();

        for (String receiver : receivers) {
            Text textNode = new Text(receiver + "\n");
            selectedReceiverTextFlow.getChildren().add(textNode);
        }
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
        mailBox.addEmail(new Email("1", "mathis.pipart@gmail.com", List.of("example1@mail.com"), "Sujet 1", "Contenu 1", LocalDateTime.now()));
        mailBox.addEmail(new Email("2", "mathis.pipart@free.fr", Arrays.asList("example2@mail.com", "exampleA@mail.com", "example7@mail.com", "exampleE@mail.com"), "Sujet 2", "Contenu 2", LocalDateTime.now()));
        mailBox.addEmail(new Email("3", "paul.zerial@edu.esiee.fr", Arrays.asList("example3@mail.com", "exampleB@mail.com", "exampleC@mail.com"), "Sujet 3", "Contenu 3", LocalDateTime.now()));
        mailBox.addEmail(new Email("4", "alice.durand@gmail.com", List.of("example4@mail.com"), "Sujet 4", "Contenu 4", LocalDateTime.now()));
        mailBox.addEmail(new Email("5", "julien.martin@orange.fr", Arrays.asList("example5@mail.com", "exampleD@mail.com"), "Sujet 5", "Contenu 5", LocalDateTime.now()));
        mailBox.addEmail(new Email("6", "emma.lefevre@hotmail.com", List.of("example6@mail.com"), "Sujet 6", "Contenu 6", LocalDateTime.now()));
        mailBox.addEmail(new Email("7", "lucas.bernard@edu.univ.fr", Arrays.asList("example7@mail.com", "exampleE@mail.com"), "Sujet 7", "Contenu 7", LocalDateTime.now()));
        mailBox.addEmail(new Email("8", "charlotte.dubois@gmail.com", Arrays.asList("example8@mail.com", "exampleF@mail.com", "exampleG@mail.com"), "Sujet 8", "Contenu 8", LocalDateTime.now()));
        mailBox.addEmail(new Email("9", "nicolas.perrin@yahoo.fr", List.of("example9@mail.com"), "Sujet 9", "Contenu 9", LocalDateTime.now()));
        mailBox.addEmail(new Email("10", "lea.moreau@laposte.net", List.of("example10@mail.com"), "Sujet 10", "Contenu 10", LocalDateTime.now()));
        mailBox.addEmail(new Email("11", "marie.dupont@gmail.com", Arrays.asList("example11@mail.com", "exampleH@mail.com"), "Sujet 11", "Contenu 11", LocalDateTime.now()));
        mailBox.addEmail(new Email("12", "quentin.leroy@hotmail.fr", Arrays.asList("example12@mail.com", "exampleI@mail.com"), "Sujet 12", "Contenu 12", LocalDateTime.now()));
        mailBox.addEmail(new Email("13", "sophie.giraud@edu.univ.fr", List.of("example13@mail.com"), "Sujet 13", "Contenu 13", LocalDateTime.now()));
        mailBox.addEmail(new Email("14", "antoine.roche@orange.fr", Arrays.asList("example14@mail.com", "exampleJ@mail.com"), "Sujet 14", "Contenu 14", LocalDateTime.now()));
        mailBox.addEmail(new Email("15", "claire.benoit@yahoo.com", Arrays.asList("example15@mail.com", "exampleK@mail.com", "exampleL@mail.com"), "Sujet 15", "Contenu 15", LocalDateTime.now()));
    }
}