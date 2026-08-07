package com.golden.geese;

/**
 * Defines the operations required to manage the application's current
 * user session. A session stores the currently signed-in user,
 * reports whether a user is signed in, and supports logging out.
 */
public interface Session {
    /**
     * Returns the user currently associated with this session.
     *
     * @return the current user, or {@code null} when no user is signed in
     */
    User getCurrentUser();

    /**
     * Sets the user currently associated with this session.
     *
     * @param user the user to store as the current user
     */
    void setCurrentUser(User user);

    /**
     * Ends the current session and removes the signed-in user.
     */
    void logout();

    /**
     * Indicates whether a user is currently signed in.
     *
     * @return {@code true} when the session contains a current user;
     *         otherwise {@code false}
     */
    boolean isLoggedIn();
}
