package com.golden.geese;

public class Save extends Interaction {

    Artifact artifact;
    public Save(User author, Artifact artifact) {
        super(author);
        this.artifact = artifact;
    }

}
