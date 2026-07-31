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

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    // The properties of a comment
    private String commentId;
    private String parentId;
    private String uid;
    private String username;
    private String text;
    private long timestamp;
    private boolean edited;
    private List<com.golden.geese.Comment> replies;
    private List<User> likeCount;

    // Constructor
    public Comment() {
        this.timestamp = System.currentTimeMillis(); // Automatically sets to current time
        this.edited = false;
        this.replies = new ArrayList<com.golden.geese.Comment>(0);
        this.likeCount = new ArrayList<User>(0);
    }

    public Comment(String uid, String username, String text) {
        this();
        this.uid = uid;
        this.username = username;
        this.text = text;
    }

    public Comment(User author, String text) {
        this(null, author == null ? null : author.getUsername(), text);
    }

    // Getters
    @Exclude
    public String getCommentId() {
        return commentId;
    }

    @Exclude
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Exclude
    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void editText(String text) {
        this.text = text;
        this.edited = true;
        setTimestamp();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    @Exclude
    public List<com.golden.geese.Comment> getReplies() {
        return replies;
    }

    @Exclude
    public void setReplies(List<com.golden.geese.Comment> replies) {
        this.replies = replies == null ? new ArrayList<com.golden.geese.Comment>(0) : replies;
    }

    @Exclude
    public List<User> getLikeCount() {
        return likeCount;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", username='" + username + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", edited=" + edited +
                ", replies=" + replies.size() +
                '}';
    }
}
