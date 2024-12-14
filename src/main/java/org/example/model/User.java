package org.example.model;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String email;
    private MailBox mailBox;

    public User(String email) {
        this.email = email;
        this.mailBox = new MailBox();
    }

    public String getEmail() {
        return email;
    }

    public MailBox getMailBox() {
        return mailBox;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMailBox(MailBox mailBox) {
        this.mailBox = mailBox;
    }
}
