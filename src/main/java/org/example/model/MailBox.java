package org.example.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MailBox implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // ObservableList utilisée pour l'interface utilisateur
    private transient ObservableList<Email> emails;

    public MailBox() {
        // Initialise l'ObservableList pour la vue
        this.emails = FXCollections.observableArrayList();
    }

    public ObservableList<Email> getEmails() {
        return emails;
    }

    public void addEmail(Email email) {
        emails.add(email);
    }

    public void deleteEmail(Email email) {
        emails.remove(email);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // Convertit l'ObservableList en ArrayList pour la sérialisation
        oos.writeObject(new ArrayList<>(emails));
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        // Reconstruit l'ObservableList à partir de la liste désérialisée
        List<Email> emailList = (List<Email>) ois.readObject();
        this.emails = FXCollections.observableArrayList(emailList).sorted(Email::compareTo);
    }
}
