package org.example.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Email implements Serializable, Comparable<Email>  {
    @Serial
    private final static long serialVersionUID = 1L;
    private final int id;
    private final String sender;
    private final List<String> receiver;
    private final String subject;
    private final String content;
    private final LocalDateTime timestamp;

    public Email(int id, String sender, List<String>  receiver, String subject, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public List<String>  getReceiver() {
        return receiver;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Email email = (Email) obj;

        return this.id == email.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id); // Utilise les mêmes champs que dans equals
    }

    @Override
    public int compareTo(Email other) {
        return this.timestamp.compareTo(other.getTimestamp());
    }
}


