package com.golden.geese;

/**
 * Represents a regular user without administrator privileges.
 */
public class RegularUser extends User {

    /**
     * Creates a regular user using the default values provided by the
     * {@link User} default constructor.
     */
    public RegularUser() {
        super();
    }

    /**
     * Creates a regular user with a specified username and profile-picture
     * value.
     *
     * @param username the user's username
     * @param pfp the user's profile-picture value or identifier
     */
    public RegularUser(String username, String pfp) {
        super(username, pfp);
    }

    /**
     * Indicates whether this user is an administrator.
     *
     * @return {@code false}, since a regular user is not an administrator
     */
    @Override
    public boolean isAdmin() {
        return false;
    }

    /**
     * Indicates whether this user has permission to add, edit, or remove
     * artifacts.
     *
     * @return {@code false}, since regular users cannot manage artifacts
     */
    @Override
    public boolean canManageArtifacts() {
        return false;
    }

    /**
     * Determines whether this user can delete the supplied comment.
     *
     * @param comment the comment potentially being deleted
     * @return {@code true} when the comment is not {@code null} and was
     *         authored by this user; otherwise {@code false}
     */
    @Override
    public boolean canDelete(Comment comment) {
        return comment != null && comment.isAuthoredBy(this);
    }
}
