/*
 * Comment
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
import java.util.ArrayList;
import java.util.List;

public class Comment {
    // The properties of a comment
    private User authorId;
    private String text;
    private LocalDateTime timestamp;
    private List<User> likeCount;
    private boolean edited;
    private List<com.golden.geese.Comment> replies;

    // Constructor
    public Comment(User authorId, String text) {
        this.authorId = authorId;
        this.text = text;
        this.timestamp = LocalDateTime.now(); // Automatically sets to current time
        this.likeCount = new ArrayList<User>(0);
        this.edited = false;
        this.replies = new ArrayList<com.golden.geese.Comment>(0);
    }

    // Getters
    public User getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text){
        edited = true;
        this.text = text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(){
        this.timestamp = LocalDateTime.now();
    }

    public List<User> getLikeCount() {
        return likeCount;
    }
    public boolean isEdited(){
        return edited;
    }
    public List<com.golden.geese.Comment> getReplies(){
        return replies;
    }

}

