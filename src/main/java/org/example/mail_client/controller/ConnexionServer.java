package org.example.mail_client.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
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
}
