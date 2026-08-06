package com.golden.geese;

import java.util.Objects;

/**
 * Represents a user of the application. A user stores identifying
 * information, including a unique identifier, username, email address,
 * and profile-picture value.
 */
public abstract class User {
    private String uid;
    private String username;
    private String email;
    private String pfp;

    /**
     * Creates a user with default username and profile-picture values.
     */
    public User(){
        this.username = "Anonymous User";
        this.pfp = "https://pbs.twimg.com/media/FeToVEqX0Acjv0i.png";
    }

    /**
     * Creates a user with a specified username and profile-picture value.
     *
     * @param username the user's username
     * @param pfp the user's profile-picture URL, path, or identifier
     */
    public User(String username, String pfp){
        this();
        this.username = username;
        this.pfp = pfp;
    }

    /**
     * Returns the user's unique identifier.
     *
     * @return the user's unique identifier
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param uid the unique identifier to assign
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns the user's username.
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the user's email address.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to assign
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's profile-picture value.
     *
     * @return the profile-picture URL, path, or identifier
     */
    public String getPfp() {
        return pfp;
    }

    /**
     * Sets the user's profile-picture value.
     *
     * @param pfp the profile-picture URL, path, or identifier to assign
     */
    public void setPfp(String pfp) {
        this.pfp = pfp;
    }

    /**
     * Determines whether this user is equal to another object. Two users
     * are considered equal when they have equal usernames and
     * profile-picture values.
     *
     * @param o the object to compare with this user
     * @return {@code true} when the object is a user with the same username
     *         and profile-picture value; otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(username, user.username) && Objects.equals(pfp, user.pfp);
    }

    /**
     * Returns a hash code based on the user's username and profile-picture
     * value.
     *
     * @return the user's hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, pfp);
    }

    /**
     * Returns a string containing the user's main properties.
     *
     * @return a string representation of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", pfp='" + pfp + '\'' +
                '}';
    }

    /**
     * Indicates whether this user has administrator privileges.
     *
     * @return {@code true} when the user is an administrator;
     *         otherwise {@code false}
     */
    public abstract boolean isAdmin();

    /**
     * Indicates whether this user can add, edit, or remove artifacts.
     *
     * @return {@code true} when the user can manage artifacts;
     *         otherwise {@code false}
     */
    public abstract boolean canManageArtifacts();

    /**
     * Indicates whether this user can delete the specified comment.
     *
     * @param comment the comment potentially being deleted
     * @return {@code true} when the user has permission to delete the comment;
     *         otherwise {@code false}
     */
    public abstract boolean canDelete(Comment comment);
}
