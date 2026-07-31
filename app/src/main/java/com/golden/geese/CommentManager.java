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

public class CommentManager extends InteractionManager<Comment> {
    public CommentManager() {
    }

    public void edit(User user, Comment comment, String text){
        if(comment.getAuthor().equals(user)) {
            comment.setText(text);
        }
    }

    @Override
    public void add(User user, Comment comment) {
        if(comment.getAuthor().equals(user)) {
            interactions.add(comment);
        }
    }

    @Override
    public void delete(User user, Comment comment) {
        if(comment.canEdit(user)){
            interactions.remove(comment);
        }
    }
}

