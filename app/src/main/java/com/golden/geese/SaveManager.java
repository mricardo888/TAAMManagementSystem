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

public class SaveManager extends InteractionManager<Save> {

    public SaveManager() {
        super();
    }

    public void toggleSave(User user, Artifact artifact){
        for(Save s : interactions) {
            if(s.getAuthor().equals(user)) {
                delete(user, s);
                return;
            }
        }
        Save save = new Save(user, artifact);
        add(user, save);
    }

    @Override
    public void add(User user, Save save) {
        if(save.getAuthor().equals(user)) {
            interactions.add(save);
            user.save(save.artifact);
        }
    }

    @Override
    public void delete(User user, Save save) {
        if(save.getAuthor().equals(user)) {
            interactions.remove(save);
            user.unsave((Artifact) save.artifact);
        }
    }
}

