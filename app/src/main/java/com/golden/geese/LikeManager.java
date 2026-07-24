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

public class LikeManager {

    private static com.golden.geese.LikeManager instance;
    private LikeManager() {
    }
    public static com.golden.geese.LikeManager getInstance(){
        if(instance == null){
            instance = new com.golden.geese.LikeManager();
        }
        return instance;
    }

    public void toggleLikes(Artifact artifact, User user){

        if(user.getLikedArtifacts().contains(artifact)){
            user.getLikedArtifacts().remove(artifact);
            artifact.likes--;
        } else{
            user.getLikedArtifacts().add(artifact);
            artifact.likes++;
        }
    }
}

