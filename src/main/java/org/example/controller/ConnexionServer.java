package org.example.controller;

import org.example.model.Email;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ConnexionServer {
    private Socket socket;
    private ObjectOutputStream outStream;
    private BufferedReader inStream;

    // Méthode pour démarrer le client et établir la connexion au serveur
    public void startClient() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            System.out.println("Host name: " + hostName);
            socket = new Socket(hostName, 8189);

            System.out.println("Connected to the server.\n");

            // Initialiser les flux
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Lire le message initial du serveur
            String line = inStream.readLine();
            System.out.println("Server says: " + line);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour envoyer un email au serveur
    public boolean sendEmail(Email email) {
        try {
            // Vérifier si le socket est connecté, sinon reconnecter
            if (socket == null || socket.isClosed()) {
                System.out.println("Socket is not connected. Attempting to reconnect...");
                startClient();
            }

            // Envoyer l'objet Email
            outStream.writeObject(email);
            outStream.flush();
            System.out.println("Email sent to server: " + email);

            // Lire la réponse du serveur
            String response = inStream.readLine();
            System.out.println("Server response: " + response);

            // Déterminer si l'envoi a réussi
            return "Mail received successfully.".equals(response);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour fermer proprement la connexion avec le serveur
    public void closeClientConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                outStream.close();
                inStream.close();
                socket.close();
                System.out.println("Client connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
