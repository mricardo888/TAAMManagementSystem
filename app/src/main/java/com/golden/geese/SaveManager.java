/*
 * SaveManager
 * Version 1.0
 * Bob Zhao July 22, 2026
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

public class SaveManager {

    private static com.golden.geese.SaveManager instance;
    private SaveManager() {
    }
    public static com.golden.geese.SaveManager getInstance(){
        if(instance == null){
            instance = new com.golden.geese.SaveManager();
        }
        return instance;
    }

    public void toggleSaves(Artifact artifact, User user){

        if(user.getSavedArtifacts().contains(artifact)){
            user.getSavedArtifacts().remove(artifact);
            artifact.saves--;
        } else{
            user.getSavedArtifacts().add(artifact);
            artifact.saves++;
        }
    }
}

