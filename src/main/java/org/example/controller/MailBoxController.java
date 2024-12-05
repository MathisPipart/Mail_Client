package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.MailClientApplication;
import org.example.model.Email;
import org.example.model.MailBox;
import org.example.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MailBoxController {
    @FXML
    private TableView<Email> emailTable;

    @FXML
    private TableColumn<Email, String> emailColumn;

    @FXML
    private Label mailName, selectedSenderLabel, selectedSubjectLabel, selectedDateLabel, fromLabel, toLabel, connectedLabel;

    @FXML
    private TextFlow selectedReceiverTextFlow, selectedContentTextFlow;

    @FXML
    private Button newMailButton, replyButton, replyAllButton, forwardButton, deleteButton;

    @FXML
    private ScrollPane receiverScrollPane;

    private User user;

    private final MailBox mailBox = new MailBox();

    private Email currentMail;

    ConnexionServer connexionServer = ConnexionServer.getInstance();;

    public void setUser(User user) {
        this.user = user;

        // Configurer l'affichage pour l'utilisateur
        mailName.setText(user.getEmail());

        // Récupérer les emails pour l'utilisateur depuis le serveur
        List<Email> retrievedEmails = connexionServer.retrieveEmails(user);

        // Ajouter les emails récupérés à la MailBox de l'utilisateur
        if (retrievedEmails != null && !retrievedEmails.isEmpty()) {
            user.getMailBox().getEmails().setAll(retrievedEmails); // Mise à jour complète
        }

        // Mettre à jour la table avec les emails de la MailBox
        emailTable.setItems(user.getMailBox().getEmails());


        // Démarrer le polling pour vérifier les nouveaux emails
        updateList();
    }

    public void updateList() {
        new Thread(() -> {
            while (true) { // Si une gestion d'arrêt est requise, ajoute une condition pour sortir de la boucle
                try {
                    // Récupérer les emails pour l'utilisateur depuis le serveur
                    List<Email> retrievedEmails = connexionServer.retrieveEmails(user);

                    // Trier les emails par ordre décroissant de date
                    if (retrievedEmails != null && !retrievedEmails.isEmpty()) {
                        retrievedEmails.sort((email1, email2) -> email2.getTimestamp().compareTo(email1.getTimestamp()));
                    }

                    // Mettre à jour la table dans l'interface utilisateur
                    Platform.runLater(() -> {
                        if (retrievedEmails != null && !retrievedEmails.isEmpty()) {
                            user.getMailBox().getEmails().setAll(retrievedEmails); // Mise à jour complète triée
                            emailTable.setItems(user.getMailBox().getEmails()); // Mettre à jour la table
                        }
                    });

                    // Attendre 5 secondes avant de relancer la récupération
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Sortir du thread si interruption
                }
            }
        }).start();
    }


    @FXML
    public void initialize() {
        listenerOnClickListMail();
        cellsInitialisation();
        enableAllButton();

        emailTable.setPlaceholder(new Label("No email to display."));
    }

    @FXML
    public void openNewMailStage() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("newMail-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 598, 488);

        NewMailController controller = fxmlLoader.getController();
        controller.setCurrentUser(user);
        controller.setMailBoxController(this);

        Stage newMailStage = new Stage();
        newMailStage.setTitle("New Mail");
        newMailStage.setScene(scene);
        newMailStage.show();
    }

    private void listenerOnClickListMail(){
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null){
                currentMail = newSelection;
                // Mettre à jour le label avec le sender de l'email sélectionné
                selectedSenderLabel.setText(newSelection.getSender());
                updateReceiverTextFlow(newSelection.getReceiver());
                selectedSubjectLabel.setText(newSelection.getSubject());
                selectedDateLabel.setText(formatDate(newSelection.getTimestamp()));

                selectedContentTextFlow.getChildren().clear();
                Text contentText = new Text(newSelection.getContent());
                selectedContentTextFlow.getChildren().add(contentText);

                setVisible();
            }
        });
    }

    private void setVisible(){
        fromLabel.setVisible(true);
        toLabel.setVisible(true);
        replyButton.setVisible(true);
        replyAllButton.setVisible(true);
        forwardButton.setVisible(true);
        deleteButton.setVisible(true);
        selectedContentTextFlow.setVisible(true);
        receiverScrollPane.setVisible(true);
    }

    @FXML
    private void updateReceiverTextFlow(List<String> receivers) {
        selectedReceiverTextFlow.getChildren().clear();

        for (String receiver : receivers) {
            Text textNode = new Text(receiver + "\n");
            selectedReceiverTextFlow.getChildren().add(textNode);
        }
    }

    private void cellsInitialisation() {
        emailTable.setRowFactory(tv -> new TableRow<Email>() {
            @Override
            protected void updateItem(Email item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-background-color: white;"); // Set background color for empty rows
                    setText(null);
                } else {
                    setStyle("");
                }
            }
        });

        // Configure CellFactory to display email information
        emailColumn.setCellFactory(column -> new TableCell<Email, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); // No content for empty cells
                    setText(null);
                } else {
                    // Retrieve the email for the current row
                    Email email = (Email) getTableRow().getItem();

                    Label fromLabel = new Label("From: " + email.getSender());
                    fromLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label subjectLabel = new Label("Subject: " + email.getSubject());
                    subjectLabel.setStyle("-fx-font-weight: bold;");

                    Label contentLabel = new Label(email.getContent());
                    contentLabel.setStyle("-fx-text-fill: grey;");

                    Button deleteButton = new Button("Delete");
                    deleteButton.setPrefWidth(50); // Set fixed button width
                    deleteButton.setOnAction(event -> listenerDeleteMailViaList(email));
                    enableHoverEffect(deleteButton);

                    Region spacerBefore = new Region();
                    HBox.setHgrow(spacerBefore, Priority.ALWAYS);
                    Region spacerAfter = new Region();
                    spacerAfter.setMinWidth(20);

                    VBox vbox = new VBox(fromLabel, subjectLabel, contentLabel);
                    vbox.setSpacing(5);

                    HBox hbox = new HBox(vbox, spacerBefore, deleteButton, spacerAfter);
                    hbox.setSpacing(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Disable horizontal scrollbar
        emailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void addMail(){
        mailBox.addEmail(new Email(1, "mathis.pipart@gmail.com", List.of("example1@mail.com"), "Sujet 1", "Contenu 1", LocalDateTime.now()));
        mailBox.addEmail(new Email(2, "mathis.pipart@free.fr", Arrays.asList("example2@mail.com", "exampleA@mail.com", "example7@mail.com", "exampleE@mail.com"), "Sujet 2", "Contenu 2", LocalDateTime.now()));
        mailBox.addEmail(new Email(3, "paul.zerial@edu.esiee.fr", Arrays.asList("example3@mail.com", "exampleB@mail.com", "exampleC@mail.com"), "Sujet 3", "Contenu 3", LocalDateTime.now()));
        mailBox.addEmail(new Email(4, "alice.durand@gmail.com", List.of("example4@mail.com"), "Sujet 4", "Contenu 4", LocalDateTime.now()));
        mailBox.addEmail(new Email(5, "julien.martin@orange.fr", Arrays.asList("example5@mail.com", "exampleD@mail.com"), "Sujet 5", "Contenu 5", LocalDateTime.now()));
        mailBox.addEmail(new Email(6, "emma.lefevre@hotmail.com", List.of("example6@mail.com"), "Sujet 6", "Contenu 6", LocalDateTime.now()));
        mailBox.addEmail(new Email(7, "lucas.bernard@edu.univ.fr", Arrays.asList("example7@mail.com", "exampleE@mail.com"), "Sujet 7", "Contenu 7", LocalDateTime.now()));
        mailBox.addEmail(new Email(8, "charlotte.dubois@gmail.com", Arrays.asList("example8@mail.com", "exampleF@mail.com", "exampleG@mail.com"), "Sujet 8", "Contenu 8", LocalDateTime.now()));
        mailBox.addEmail(new Email(9, "nicolas.perrin@yahoo.fr", List.of("example9@mail.com"), "Sujet 9", "Contenu 9", LocalDateTime.now()));
        mailBox.addEmail(new Email(10, "lea.moreau@laposte.net", List.of("example10@mail.com"), "Sujet 10", "Contenu 10", LocalDateTime.now()));
        mailBox.addEmail(new Email(11, "marie.dupont@gmail.com", Arrays.asList("example11@mail.com", "exampleH@mail.com"), "Sujet 11", "Contenu 11", LocalDateTime.now()));
        mailBox.addEmail(new Email(12, "quentin.leroy@hotmail.fr", Arrays.asList("example12@mail.com", "exampleI@mail.com"), "Sujet 12", "Contenu 12", LocalDateTime.now()));
        mailBox.addEmail(new Email(13, "sophie.giraud@edu.univ.fr", List.of("example13@mail.com"), "Sujet 13", "Contenu 13", LocalDateTime.now()));
        mailBox.addEmail(new Email(14, "antoine.roche@orange.fr", Arrays.asList("example14@mail.com", "exampleJ@mail.com"), "Sujet 14", "Contenu 14", LocalDateTime.now()));
        mailBox.addEmail(new Email(15, "claire.benoit@yahoo.com", Arrays.asList("example15@mail.com", "exampleK@mail.com", "exampleL@mail.com"), "Sujet 15", "Contenu 15", LocalDateTime.now()));
        user.setMailBox(mailBox);
    }

    private String formatDate(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy, HH:mm:ss");
        return timestamp.format(formatter);
    }

    @FXML
    public void replyMailStage() throws Exception {
        if(currentMail != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("newMail-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 598, 488);

            NewMailController controller = fxmlLoader.getController();
            controller.setUserMail(user.getEmail());

            controller.setSendTo(currentMail.getSender()+";");
            controller.setSubject("RE: " + currentMail.getSubject());

            Stage newMailStage = new Stage();
            newMailStage.setTitle("Reply Mail");
            newMailStage.setScene(scene);
            newMailStage.setOnShown(event -> controller.getContent().requestFocus());
            newMailStage.show();
        }
        else{
            System.out.println("No mail selected");
        }
    }

    @FXML
    public void replyAllMailStage() throws Exception {
        if(currentMail != null) {
        FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("newMail-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 598, 488);

        NewMailController controller = fxmlLoader.getController();
        controller.setUserMail(user.getEmail());


        String allReceivers = currentMail.getSender()+ ";  ";
        for (String receiver : currentMail.getReceiver()) {
            if(!user.getEmail().equals(receiver))
                allReceivers += receiver + ";  ";
        }
        controller.setSendTo(allReceivers);

        controller.setSubject("RE: " + currentMail.getSubject());

        Stage newMailStage = new Stage();
        newMailStage.setTitle("Reply All Mail");
        newMailStage.setScene(scene);
        newMailStage.setOnShown(event -> controller.getContent().requestFocus());
        newMailStage.show();
        }
        else{
            System.out.println("No mail selected");
        }
    }

    @FXML
    public void forwardMailStage() throws Exception {
        if(currentMail != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(MailClientApplication.class.getResource("newMail-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 598, 488);

            NewMailController controller = fxmlLoader.getController();
            controller.setUserMail(user.getEmail());

            controller.setSubject("FW: " + currentMail.getSubject());
            controller.setContent(currentMail.getContent());

            Stage newMailStage = new Stage();
            newMailStage.setTitle("Reply Mail");
            newMailStage.setScene(scene);
            newMailStage.setOnShown(event -> controller.getSendTo().requestFocus());
            newMailStage.show();
        }
        else{
            System.out.println("No mail selected");
        }
    }

    @FXML
    public void deleteMail(){
        if(currentMail == null) {
            System.out.println("No mail selected");
        }

        if(! user.getMailBox().getEmails().isEmpty()){
            user.getMailBox().deleteEmail(currentMail);
        }
        else {
            System.out.println("No mail to delete");
        }
    }

    @FXML
    public void listenerDeleteMailViaList(Email email){
        currentMail = email;
        deleteMail();
    }

    public void enableAllButton(){
        enableHoverEffect(newMailButton);
        enableHoverEffect(replyButton);
        enableHoverEffect(replyAllButton);
        enableHoverEffect(forwardButton);
        enableHoverEffect(deleteButton);
    }

    public void enableHoverEffect(Button button) {
        button.setOnMouseEntered(event -> button.setStyle("-fx-cursor: hand;")); // Change le curseur en main
        button.setOnMouseExited(event -> button.setStyle("")); // Réinitialise le style
    }
}