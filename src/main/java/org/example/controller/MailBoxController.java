package org.example.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.ArrayList;
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

    ObservableList<Email> emailList = FXCollections.observableArrayList();

    private User user;
    private final MailBox mailBox = new MailBox();

    private Email currentMail;

    ConnexionServer connexionServer = ConnexionServer.getInstance();;
    private volatile boolean keepUpdating = true;


    @FXML
    public void initialize() {
        listenerOnClickListMail();
        cellsInitialisation();
        enableAllButton();

        emailTable.setPlaceholder(new Label("No email to display."));
    }

    private void listenerOnClickListMail(){
        emailTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null){
                currentMail = newSelection;

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

    private void cellsInitialisation() {
        emailTable.setRowFactory(tv -> new TableRow<Email>() {
            @Override
            protected void updateItem(Email item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("-fx-background-color: white;");
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
                    setGraphic(null);
                    setText(null);
                } else {
                    // Retrieve the email for the current row
                    Email email = (Email) getTableRow().getItem();

                    Label fromLabel = new Label("From: " + email.getSender());
                    fromLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label subjectLabel = new Label("Subject: " + email.getSubject());
                    subjectLabel.setStyle("-fx-font-weight: bold;");


                    String content = email.getContent();
                    String firstLine = content.contains("\n") ? content.substring(0, content.indexOf("\n")) : content;
                    Label contentLabel = new Label(firstLine);
                    contentLabel.setStyle("-fx-text-fill: grey;");


                    Button deleteButton = new Button("Delete");
                    deleteButton.setPrefWidth(50);
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



    public void setUser(User user) {
        this.user = user;
        mailName.setText(user.getEmail());

        // Retrieve emails for the user from the server
        List<Email> retrievedEmails = connexionServer.retrieveEmails(user);

        // Add recovered emails to the user's MailBox
        if (retrievedEmails != null && !retrievedEmails.isEmpty()) {
            user.getMailBox().getEmails().setAll(retrievedEmails); // Mise à jour complète
        }

        // Update table with MailBox emails
        emailList.addAll(user.getMailBox().getEmails());
        emailTable.setItems(emailList);


        // Start polling to check for new emails
        updateListLoop();
    }

    public void updateListLoop() {
        new Thread(() -> {
            while (keepUpdating) {
                try {
                    this.updateList();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            }).start();
    }

    public void updateList() {
        updateConnexionLabel();

        List<Email> retrievedEmails = connexionServer.retrieveEmails(user);

        if (!connexionServer.isConnected() || retrievedEmails == null) {
            return;
        }

        // Sort emails
        if (retrievedEmails != null && !retrievedEmails.isEmpty()) {
            retrievedEmails.sort((email1, email2) -> email2.getTimestamp().compareTo(email1.getTimestamp()));
        }

        // Update table in user interface
        Platform.runLater(() -> {
            if (retrievedEmails != null) {
                // Add to "emailList" items that are not already inserted in
                retrievedEmails.forEach(e -> {
                    if (!emailList.contains(e)) {
                        emailList.addFirst(e);
                    }
                });

                final List<Email> toRemove = new ArrayList<>();

                emailList.forEach(e -> {
                    if (!retrievedEmails.contains(e)) {
                        toRemove.add(e);
                    }
                });

                toRemove.forEach(e -> emailList.remove(e));
            }
        });
    }

    public void stopUpdating() {
        keepUpdating = false;
    }



    public void updateConnexionLabel(){
        boolean isConnected = connexionServer.isConnected();
        String newLabelState = isConnected ? "Connected" : "Not Connected";

        if (!newLabelState.equals(connectedLabel.getText())) {
            Platform.runLater(() -> {
                connectedLabel.setText(newLabelState);
            });
        }
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

    private String formatDate(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy, HH:mm:ss");
        return timestamp.format(formatter);
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
            newMailStage.setTitle("Forward Mail");
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
        if (currentMail == null) {
            System.out.println("No mail selected");
        }

        if (!user.getMailBox().getEmails().isEmpty()){
            connexionServer.deleteEmail(user, currentMail.getId());
            user.getMailBox().deleteEmail(currentMail);
            this.updateList();
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

}