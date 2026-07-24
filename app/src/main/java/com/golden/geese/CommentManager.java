/*
 * CommentManager
 * Version 1.0
 * Bob Zhao July 23, 2026
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

public class CommentManager {
    private static com.golden.geese.CommentManager instance;
    private CommentManager() {
    }
    public static com.golden.geese.CommentManager getInstance(){
        if(instance == null){
            instance = new com.golden.geese.CommentManager();
        }
        return instance;
    }

    public Comment addComment(Artifact artifact, User user, String text){
        Comment comment = new Comment(user, text);
        user.getComments().add(comment);
        artifact.getComments().add(comment);
        return comment;
    }
    public void addComment(Artifact artifact, User user, Comment comment){
        user.getComments().add(comment);
        artifact.getComments().add(comment);
    }

    public void editComment(User user, Comment comment, String text){
        if(user.getComments().contains(comment)){
            comment.setText(text);
            comment.setTimestamp();
        }
    }

    public void toggleLikeComment(User user, Comment comment){
        if(comment.getLikeCount().contains(user)){
            comment.getLikeCount().remove(user);

        }else{
            comment.getLikeCount().add(user);
        }

    }

    public void deleteComment(Artifact artifact, User user, Comment comment){
        if(user.isAdmin() || user.getComments().contains(comment)){
            artifact.getComments().remove(comment);
            user.getComments().remove(comment);
            for(Comment r : comment.getReplies()){
                deleteComment(artifact, r.getAuthorId(), r);
            }
        }
    }

    public void reply(Artifact artifact, User user, Comment replyTo, String text){
        Comment comment = addComment(artifact, user, text);
        replyTo.getReplies().add(comment);
    }
    public void reply(Artifact artifact, User user, Comment replyTo, Comment replyComment){
        addComment(artifact, user, replyComment);
        replyTo.getReplies().add(replyComment);
    }
}

