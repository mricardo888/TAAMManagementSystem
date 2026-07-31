/*
 * Artifact
 * Version 1.0
 * Bob Zhao July 17, 2026
 *
 * This code is provided as part of the coursework for CSCB07H3
 * at the University of Toronto.
 *
 * Unauthorized reproduction, distribution, or sharing of this code is strictly
 * prohibited and constitutes a violation of the University of
 * Toronto Code of Behaviour on Academic Matters.
 *
 */
package com.golden.geese;
import java.time.LocalDateTime;

public class Comment {

    // The properties of a comment
    private String authorId;
    private String text;
    private LocalDateTime timestamp;
    private int likeCount;

    // Constructor
    public Comment(String authorId, String text) {
        this.authorId = authorId;
        this.text = text;
        this.timestamp = LocalDateTime.now(); // Automatically sets to current time
        this.likeCount = 0;
    }

    // Getters
    public String getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    // Setters / Modifiers
    public void incrementLikes() {
        this.likeCount++;
    }
}
