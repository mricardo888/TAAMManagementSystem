package com.golden.geese;

import java.util.Objects;

public abstract class User {
    private String uid;
    private String username;
    private String email;
    private String pfp;

    public User(){
        this.username = "Anonymous User";
        this.pfp = "https://pbs.twimg.com/media/FeToVEqX0Acjv0i.png";
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPfp() {
        return pfp;
    }

    public void setPfp(String pfp) {
        this.pfp = pfp;
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
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", pfp='" + pfp + '\'' +
                '}';
    }

    public abstract boolean isAdmin();

    public abstract boolean canManageArtifacts();

    public abstract boolean canDelete(Comment comment);
}
