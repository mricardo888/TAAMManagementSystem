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

import java.util.List;

public class Comment extends Interaction implements Likeable {
    // The properties of a comment
    private String text;
    private LikeManager likeManager;
    private CommentManager subCommentManager;
    private boolean edited;

    private String id;
    private String parentId;
    private boolean isReply;

    // Constructor
    public Comment() {
        super();
        text = "";
        likeManager = new LikeManager();
        subCommentManager = new CommentManager();
        edited = false;
        id = "";
    }

    public Comment(User author, String text) {
        super(author);
        this.text = text;
        this.likeManager = new LikeManager();
        this.subCommentManager = new CommentManager();
        this.edited = false;
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

    public List<Comment> getSubComments() {
        return subCommentManager.getInteractions();
    }

    public void setCommentId(String id) {
        this.id = id;
    }

    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentId() { return parentId; }

    public boolean isReply() { return isReply; }
}
