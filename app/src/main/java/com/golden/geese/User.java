package com.golden.geese;/*
 * Artifact
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

import java.util.*;
import com.golden.geese.*;

public abstract class User {
    private List<Artifact> likedArtifacts;
    private String username;
    private String pfp;
    private List<Artifact> savedArtifacts;


    public User(){
        this.username = "Anonymous User";
        this.pfp = "https://pbs.twimg.com/media/FeToVEqX0Acjv0i.png";
        this.likedArtifacts = new ArrayList<Artifact>(0);
        this.savedArtifacts = new ArrayList<Artifact>(0);
    }

    public User(String username, String pfp){
        this();
        this.username = username;
        this.pfp = pfp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPfp() {
        return pfp;
    }

    public void setPfp(String pfp) {
        this.pfp = pfp;
    }

    public void like(Artifact artifact) {
        likedArtifacts.add(artifact);
    }

    public void unlike(Artifact artifact) {
        likedArtifacts.remove(artifact);
    }

    public void save(Artifact artifact) {
        savedArtifacts.add(artifact);
    }

    public void unsave(Artifact artifact) {
        savedArtifacts.remove(artifact);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(username, user.username) && Objects.equals(pfp, user.pfp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, pfp);
    }

    @Override
    public String toString() {
        return "User{" +
                "likedArtifacts=" + likedArtifacts +
                ", username='" + username + '\'' +
                ", pfp='" + pfp + '\'' +
                ", savedArtifacts=" + savedArtifacts +
                '}';
    }

    public abstract boolean isAdmin();
}
