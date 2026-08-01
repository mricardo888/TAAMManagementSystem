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

import java.util.ArrayList;
import java.util.List;

public class Comment extends Interaction implements Likeable {
    // The properties of a comment
    private String text;
    private LikeManager likeManager;
    private boolean edited;
    private String commentId;
    private String parentId;
    private List<Comment> replies;

    // Constructor
    public Comment() {
        super();
        text = "";
        likeManager = new LikeManager();
        edited = false;
        commentId = "";
        replies = new ArrayList<>();
    }

    public Comment(User author, String text) {
        super(author);
        this.text = text;
        this.likeManager = new LikeManager();
        this.edited = false;
        this.commentId = "";
        this.replies = new ArrayList<>();
    }

    // Getters

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

    public void editText(String text) {
        edited = true;
        this.text = text;
    }

    public int getLikeCount() {
        return likeManager.getNumInteractions();
    }

    public boolean isEdited(){
        return edited;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    public List<Comment> getReplies() {
        return replies;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", text='" + text + '\'' +
                ", edited=" + edited +
                ", replies=" + replies.size() +
                '}';
    }
}