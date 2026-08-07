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

/**
 * Represents a comment made by a user. A comment stores its text, edit status,
 * unique identifier, optional parent comment identifier, and any replies
 * associated with it.
 */
public class Comment extends Interaction {
    // The properties of a comment
    private String text;
    private boolean edited;
    private String commentId;
    private String parentId;
    private List<Comment> replies;

    /**
     * Creates an empty comment with default interaction information.
     */
    public Comment() {
        super();
        text = "";
        edited = false;
        commentId = "";
        replies = new ArrayList<>();
    }

    /**
     * Creates a comment with a specified author and text.
     *
     * @param author the user who created the comment
     * @param text the written content of the comment
     */
    public Comment(User author, String text) {
        super(author);
        this.text = text;
        this.edited = false;
        this.commentId = "";
        this.replies = new ArrayList<>();
    }

    /**
     * Returns the written content of the comment.
     *
     * @return the comment text
     */
    public String getText() {
        return text;
    }

    /**
     * Replaces the comment text and marks the comment as edited.
     *
     * @param text the updated comment text
     */
    public void editText(String text) {
        this.edited = true;
        this.text = text;
    }

    /**
     * Indicates whether the comment has been edited.
     *
     * @return {@code true} if the comment has been edited;
     *         otherwise {@code false}
     */
    public boolean isEdited(){
        return edited;
    }

    /**
     * Changes the edited state of the comment.
     *
     * @param edited {@code true} to mark the comment as edited;
     *               {@code false} otherwise
     */
    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    /**
     * Returns the unique identifier of the comment.
     *
     * @return the comment identifier
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * Assigns a unique identifier to the comment.
     *
     * @param commentId the identifier to assign
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Returns the identifier of the parent comment.
     *
     * @return the parent comment identifier, or an empty or {@code null} value
     *         when this is not a reply
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Assigns the identifier of the parent comment.
     *
     * @param parentId the identifier of the parent comment
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Indicates whether this comment is a reply to another comment.
     *
     * @return {@code true} when the parent identifier is neither
     *         {@code null} nor empty; otherwise {@code false}
     */
    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    /**
     * Returns the replies associated with this comment.
     *
     * @return the list of replies
     */
    public List<Comment> getReplies() {
        return replies;
    }

    /**
     * Determines whether the specified user authored this comment.
     *
     * @param user the user to compare with the comment's author
     * @return {@code true} when the supplied user and the comment author have
     *         the same non-null user identifier; otherwise {@code false}
     */
    public boolean isAuthoredBy(User user) {
        return user != null
                && user.getUid() != null
                && getAuthor() != null
                && user.getUid().equals(getAuthor().getUid());
    }

    /**
     * Returns a string containing the comment's main properties.
     *
     * @return a string representation of the comment
     */
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
