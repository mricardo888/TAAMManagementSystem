package com.golden.geese;

/**
 * Represents an administrator user with permission to manage artifacts
 * and delete comments.
 */
public class AdminUser extends User {
    /**
     * Creates an administrator user using the default values provided
     * by the {@link User} default constructor.
     */
    public AdminUser() {
        super();
    }

    /**
     * Creates an administrator user with a specified username and
     * profile-picture value.
     *
     * @param username the administrator's username
     * @param pfp the administrator's profile-picture value or identifier
     */
    public AdminUser(String username, String pfp) {
        super(username, pfp);
    }

    /**
     * Indicates whether this user is an administrator.
     *
     * @return {@code true}, since every {@code AdminUser} is an administrator
     */
    @Override
    public boolean isAdmin() {
        return true;
    }

    /**
     * Indicates whether this user has permission to add, edit, or remove
     * artifacts.
     *
     * @return {@code true}, since administrators can manage artifacts
     */
    @Override
    public boolean canManageArtifacts() {
        return true;
    }

    /**
     * Determines whether this administrator can delete the supplied comment.
     *
     * @param comment the comment potentially being deleted
     * @return {@code true} when the comment is not {@code null};
     *         otherwise {@code false}
     */
    @Override
    public boolean canDelete(Comment comment) {
        return comment != null;
    }
}
