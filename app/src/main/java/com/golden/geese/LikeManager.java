/*
 * LikeManager
 * Version 1.0
 * Bob Zhao July 20, 2026
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

public class LikeManager extends InteractionManager<Like> {
    public LikeManager() {
        super();
    }

    public void toggleLike(User user, Likeable likedObject){
        for(Like l : interactions) {
            if(l.getAuthor().equals(user)) {
                delete(user, l);
                return;
            }
        }
        Like like = new Like(user, likedObject);
        add(user, like);
    }

    @Override
    public void add(User user, Like like) {
        if(like.getAuthor().equals(user)) {
            interactions.add(like);
            if (like.likedObject instanceof Artifact) {
                user.like((Artifact) like.likedObject);
            }
        }
    }

    @Override
    public void delete(User user, Like like) {
        if(like.getAuthor().equals(user)) {
            interactions.remove(like);
            if(like.likedObject instanceof Artifact) {
                like.getAuthor().unlike((Artifact) like.likedObject);
            }
        }
    }
}

