package org.example.controller;

import org.example.model.Email;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnexionServer {
    private Socket socket;
    private ObjectOutputStream outStream;
    private BufferedReader inStream;

    public void startClient() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            socket = new Socket(hostName, 8189);

            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String welcomeMessage = inStream.readLine();
            System.out.println("Server says: " + welcomeMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean sendEmail(Email email) {
        try {
            if (socket == null || socket.isClosed()) {
                startClient();
            }

            outStream.writeObject(email);
            outStream.flush();

            String response = inStream.readLine();
            return response.startsWith("Mail received successfully with ID:");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Email> retrieveEmails(String userEmail) {
        List<Email> emails = new ArrayList<>();

        try {
            if (socket == null || socket.isClosed()) {
                startClient();
            }

            outStream.writeObject("RETRIEVE_MAILS:" + userEmail);
            outStream.flush();

            String line;
            while ((line = inStream.readLine()) != null) {
                if (line.equals("END_OF_MAILS")) {
                    break;
                }

                String[] parts = line.split(";");
                Email email = new Email(
                        Integer.parseInt(parts[0]), // ID
                        parts[1],                   // Expéditeur
                        Arrays.asList(parts[2].split(",")), // Destinataires
                        parts[3],                   // Sujet
                        parts[4],                   // Contenu
                        LocalDateTime.parse(parts[5]) // Timestamp
                );
                emails.add(email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emails;
    }


    public void closeClientConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
