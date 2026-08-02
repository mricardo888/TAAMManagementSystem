package com.golden.geese;

import java.util.*;

public abstract class User {
    private List<Artifact> likedArtifacts;
    private String uid;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
