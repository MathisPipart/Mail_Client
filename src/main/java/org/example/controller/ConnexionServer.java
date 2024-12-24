package org.example.controller;

import org.example.model.Email;
import org.example.model.User;
import org.jetbrains.annotations.NotNull;

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
    private Socket socket;
    private ObjectOutputStream outStream;
    private BufferedReader inStream;

    private static ConnexionServer instance;
    MailBoxController mailBoxController;
    private boolean connected = false;


    private ConnexionServer() {
    }

    public static synchronized ConnexionServer getInstance() {
        if (instance == null) {
            instance = new ConnexionServer();
        }
        return instance;
    }

    public void setMailBoxController(MailBoxController mailBoxController) {
        this.mailBoxController = mailBoxController;
    }


    public boolean startClient(User user) {
        try {
            if (!isConnected()) {
                String hostName = InetAddress.getLocalHost().getHostName();
                socket = new Socket(hostName, 8189);
                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true;

                String welcomeMessage = inStream.readLine();
                System.out.println("Server says: " + welcomeMessage);

                // Send user
                outStream.writeObject(user);
                outStream.flush();

                // Read the answer about the user
                String response = inStream.readLine();
                if (response != null) {
                    if (response.startsWith("Error:")) {
                        System.err.println("Server-side error : " + response);
                        closeClientConnection();
                        return false;
                    } else if (response.startsWith("User connected successfully.")) {
                        System.out.println("The user is successfully logged in.");
                        return true;
                    } else {
                        System.err.println("Unexpected server response: " + response);
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
            System.err.println("Error: Unable to connect to server.");
            connected = false;
            return false;
        } catch (IOException e) {
            System.err.println("I/O Error during connection:" + e.getMessage());
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
                startClient(user);
            }

            // Additional checks before sending data
            if (outStream == null || inStream == null) {
                throw new IllegalStateException("Flux non initialisés. Connexion invalide.");
            }

            // Send order to retrieve emails
            outStream.writeObject("RETRIEVE_MAILS:" + user.getEmail());
            outStream.flush();

            // Read emails sent by the server
            String line;
            while ((line = inStream.readLine()) != null) {
                if (line.equals("END_OF_MAILS")) {
                    break;
                }

                if (line.startsWith("Mail:")) {
                    String emailData = line.substring(5);
                    if (!emailData.isBlank()) {
                        Email email = getEmail(emailData);

                        retrievedEmails.add(email);
                        if (!user.getMailBox().getEmails().contains(email)) {
                            user.getMailBox().addEmail(email);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Connection interrupted : " + e.getMessage());
            connected = false;
            return null;
        } catch (IOException e) {
            System.err.println("Input/output error : " + e.getMessage());
            connected = false;
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return retrievedEmails;
    }

    @NotNull
    private static Email getEmail(String emailData) {
        String[] parts = emailData.split(";");
        return new Email(
                Integer.parseInt(parts[0]),
                parts[1],
                Arrays.asList(parts[2].split("\\|")),
                parts[3],
                parts[4].replaceAll("\\\\n", "\n"),
                LocalDateTime.parse(parts[5])
        );
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
                startClient(currentUser);
            }

            // Send verification command
            outStream.writeObject("CHECK_USER:" + emailToCheck);
            outStream.flush();

            // Read the answer
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
                // Inform server of disconnection
                outStream.writeObject("DISCONNECT");
                outStream.flush();

                socket.close();
                connected = false;
                System.out.println("Closed connection.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
