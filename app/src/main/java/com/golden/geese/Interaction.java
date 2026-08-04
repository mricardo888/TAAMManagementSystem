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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
