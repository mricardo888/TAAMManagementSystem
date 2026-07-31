package com.golden.geese;

import java.time.LocalDateTime;

public abstract class Interaction {
    private User author;
    private LocalDateTime timestamp;

    public Interaction() {
        author = new RegularUser();
        timestamp = LocalDateTime.now();
    }

    public Interaction(User author) {
        this.author = author;
        this.timestamp = LocalDateTime.now();
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean canEdit(User user) {
        return user.isAdmin() || author.equals(user);
    }

    public void updateTimestamp() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}