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

    public boolean startClient(User user) {
        try {
            if (!isConnected()) {
                String hostName = InetAddress.getLocalHost().getHostName();
                socket = new Socket(hostName, 8189);
                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true;

                // Lire le message de bienvenue
                String welcomeMessage = inStream.readLine();
                System.out.println("Server says: " + welcomeMessage);

                // Envoyer l'utilisateur
                outStream.writeObject(user);
                outStream.flush();

                // Lire la réponse concernant l'utilisateur
                String response = inStream.readLine();
                if (response != null) {
                    if (response.startsWith("Error:")) {
                        // L'utilisateur n'existe pas, on ferme la connexion et on retourne false
                        System.err.println("Erreur côté serveur : " + response);
                        closeClientConnection();
                        return false;
                    } else if (response.startsWith("User connected successfully.")) {
                        // L'utilisateur existe, connexion établie
                        System.out.println("L'utilisateur est connecté avec succès.");
                        return true;
                    } else {
                        // Réponse inattendue
                        System.err.println("Réponse inattendue du serveur : " + response);
                        closeClientConnection();
                        return false;
                    }
                } else {
                    closeClientConnection();
                    return false;
                }
            }
            return true;
        } catch (ConnectException e) {
            System.err.println("Erreur : Impossible de se connecter au serveur.");
            connected = false;
            return false;
        } catch (IOException e) {
            System.err.println("Erreur d'E/S lors de la connexion : " + e.getMessage());
            connected = false;
            return false;
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
                        String[] parts = emailData.split("\\|");
                        Email email = new Email(
                                Integer.parseInt(parts[0]),
                                parts[1],
                                Arrays.asList(parts[2].split(";")),
                                parts[3],
                                parts[4].replaceAll("\\\\n", "\n"), // si vous aviez remplacé les \n côté serveur
                                LocalDateTime.parse(parts[5])
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


    public boolean deleteEmail(User user, int emailId) {
        try {
            if (!isConnected()) {
                startClient(user);
            }

            String command = "DELETE_MAIL:" + user.getEmail() + "," + emailId;
            outStream.writeObject(command);
            outStream.flush();

            String response = inStream.readLine();
            System.out.println("Server response: " + response);
            return response != null && response.equals("Mail deleted successfully.");
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkUserExists(User currentUser, String emailToCheck) {
        try {
            if (!isConnected()) {
                // (Re)connexion si nécessaire
                startClient(currentUser);
            }

            // Envoyer la commande de vérification
            outStream.writeObject("CHECK_USER:" + emailToCheck);
            outStream.flush();

            // Lire la réponse
            String response = inStream.readLine();
            if (response != null) {
                if (response.startsWith("User exists")) {
                    return true;
                } else if (response.startsWith("Error: User does not exist")) {
                    return false;
                }
            }
            return false;
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
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
