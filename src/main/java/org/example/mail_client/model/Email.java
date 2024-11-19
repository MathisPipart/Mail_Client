package org.example.mail_client.model;

import java.time.LocalDateTime;

public class Email {
    private String id;
    private String sender;
    private String recipient;
    private String subject;
    private String content;
    private LocalDateTime timestamp;

    public Email(String id, String sender, String recipient, String subject, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

