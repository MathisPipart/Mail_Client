package org.example.controller;

import javafx.application.Platform;
import org.example.model.Email;
import org.example.model.MailBox;
import org.example.model.User;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
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

    private boolean connected = false;

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
            if (!isConnected()) { // Connecter seulement si le client n'est pas déjà connecté
                String hostName = InetAddress.getLocalHost().getHostName();

                // Tenter de se connecter au serveur
                socket = new Socket(hostName, 8189);
                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true; // Mise à jour de l'état

                String welcomeMessage = inStream.readLine();
                System.out.println("Server says: " + welcomeMessage);

                // Envoyer l'objet User au serveur
                outStream.writeObject(user);
                outStream.flush();
            }
        } catch (ConnectException e) {
            System.err.println("Erreur : Impossible de se connecter au serveur. Vérifiez si le serveur est actif.");
            connected = false; // Mettre l'état à "non connecté"
        } catch (SocketException e) {
            System.err.println("Connexion interrompue : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur d'E/S lors de la connexion : " + e.getMessage());
        }
    }



    public boolean sendEmail(User user, Email email) {
        try {
            if (!isConnected()) {
                startClient(user);
            }

            outStream.writeObject(email);
            outStream.flush();

            String response = inStream.readLine();
            System.out.println("Server response: " + response);
            return response != null && response.startsWith("Mail received successfully with ID:");
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
            return false;
        }
    }


    public List<Email> retrieveEmails(User user) {
        List<Email> retrievedEmails = new ArrayList<>();

        try {
            if (!isConnected()) {
                startClient(user); // Tenter de se reconnecter
            }

            // Vérification supplémentaire avant d'envoyer des données
            if (outStream == null || inStream == null) {
                throw new IllegalStateException("Flux non initialisés. Connexion invalide.");
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
                    String emailData = line.substring(5);
                    if (!emailData.isBlank()) {
                        String[] parts = emailData.split(",");
                        Email email = new Email(
                                Integer.parseInt(parts[0]), // ID
                                parts[1], // Expéditeur
                                Arrays.asList(parts[2].split(";")), // Destinataires
                                parts[3], // Sujet
                                parts[4], // Contenu
                                LocalDateTime.parse(parts[5]) // Timestamp
                        );
                        retrievedEmails.add(email);
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Connexion interrompue : " + e.getMessage());
            connected = false; // Mettre à jour l'état de connexion
        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie : " + e.getMessage());
            connected = false;
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }

        return retrievedEmails;
    }



    public boolean isConnected() {
        try {
            return socket != null && !socket.isClosed() && connected && socket.isConnected();
        } catch (Exception e) {
            return false;
        }
    }




    public void closeClientConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                connected = false;
                System.out.println("Connexion fermée.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
