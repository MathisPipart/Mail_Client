package org.example.controller;

import javafx.application.Platform;
import org.example.model.Email;
import org.example.model.MailBox;
import org.example.model.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnexionServer {
    // Instance unique de Singleton
    private static ConnexionServer instance;

    MailBoxController mailBoxController;

    public void setMailBoxController(MailBoxController mailBoxController) {
        this.mailBoxController = mailBoxController;
    }

    // Variables de connexion
    private Socket socket;
    private ObjectOutputStream outStream;
    private BufferedReader inStream;

    private volatile boolean listening = false;

    // Constructeur privé pour empêcher l'instanciation directe
    private ConnexionServer() {
    }

    // Méthode pour récupérer l'instance unique
    public static synchronized ConnexionServer getInstance() {
        if (instance == null) {
            instance = new ConnexionServer();
        }
        return instance;
    }

    public void startClient(User user) {
        try {
            if (socket == null || socket.isClosed()) {
                listening = true; // Activer l'écoute

                String hostName = InetAddress.getLocalHost().getHostName();
                socket = new Socket(hostName, 8189);

                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String welcomeMessage = inStream.readLine();
                System.out.println("Server says: " + welcomeMessage);

                // Envoyer l'objet User au serveur
                outStream.writeObject(user);
                outStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean sendEmail(User user, Email email) {
        try {
            if (socket == null || socket.isClosed()) {
                startClient(user);
            }

            outStream.writeObject(email);
            outStream.flush();

            String response = inStream.readLine();
            System.out.println("Server response: " + response);
            return response != null && response.startsWith("Mail received successfully with ID:");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Email> retrieveEmails(User user) {
        List<Email> retrievedEmails = new ArrayList<>();

        try {
            if (socket == null || socket.isClosed()) {
                startClient(user); // S'assurer que le client est connecté
            }

            // Envoyer la commande pour récupérer les emails
            outStream.writeObject("RETRIEVE_MAILS:" + user.getEmail());
            outStream.flush();

            // Lire les emails envoyés par le serveur
            String line;
            while ((line = inStream.readLine()) != null) {
                if (line.equals("END_OF_MAILS")) {
                    break; // Fin de la liste des emails
                }

                if (line.startsWith("Mail:")) {
                    String emailData = line.substring(5); // Supprimer "Mail:"

                    // Vérifier si emailData est vide
                    if (emailData.isBlank()) {
                        System.out.println("Aucun email pour l'utilisateur : " + user.getEmail());
                        continue;
                    }

                    // Diviser les données de l'email
                    try {
                        String[] parts = emailData.split(",");

                        Email email = new Email(
                                Integer.parseInt(parts[0]),                // ID
                                parts[1],                                  // Expéditeur
                                Arrays.asList(parts[2].split(";")),        // Destinataires (séparés par des ;)
                                parts[3],                                  // Sujet
                                parts[4],                                  // Contenu
                                LocalDateTime.parse(parts[5])              // Timestamp
                        );

                        retrievedEmails.add(email); // Ajouter à la liste des emails
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'analyse de l'email: " + emailData);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Message serveur : " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retrievedEmails; // Retourner la liste des emails
    }





    public void closeClientConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Connexion fermée.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
