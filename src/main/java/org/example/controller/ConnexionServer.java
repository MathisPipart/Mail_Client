package org.example.controller;

import org.example.model.Email;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class ConnexionServer {

    public void startClient() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            System.out.println("Host name: " + hostName);
            Socket socket = new Socket(hostName, 8189);

            System.out.println("Connected to the server.\n");

            try (InputStream inStream = socket.getInputStream();
                 Scanner in = new Scanner(inStream);
                 ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream())) {

                System.out.println("Waiting to receive data from the server...");

                // Lire le message initial du serveur
                String line = in.nextLine();
                System.out.println(line);

                // Préparer la liste des emails
                Vector<String> emailList = new Vector<>();
                emailList.add("alice@example.com");
                emailList.add("bob@example.com");
                emailList.add("charlie@example.com");
                emailList.add("diana@example.com");

                // Envoyer les emails au serveur
                outStream.writeObject(emailList);

                // Lire la réponse du serveur
                while (in.hasNextLine()) {
                    line = in.nextLine();
                    System.out.println("Server response: " + line);
                }
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendEmail(Email email) {
        final boolean[] success = {false};

        // Exécuter l'envoi dans un thread séparé
        Thread thread = new Thread(() -> {
            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                System.out.println("Host name: " + hostName);

                try (Socket socket = new Socket(hostName, 8189);
                     ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Envoyer l'objet Email
                    outStream.writeObject(email);
                    outStream.flush();
                    System.out.println("Email sent to server: " + email);

                    // Lire la réponse du serveur
                    String response = in.readLine();
                    System.out.println("Server response: " + response);

                    // Déterminer si l'envoi a réussi
                    success[0] = "Mail received successfully.".equals(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Démarrer le thread
        thread.setDaemon(true); // S'assure que le thread s'arrête lorsque l'application se termine
        thread.start();

        try {
            // Attendre que le thread termine son exécution (timeout pour éviter un blocage infini)
            thread.join(5000); // 5 secondes maximum
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return success[0];
    }
}
