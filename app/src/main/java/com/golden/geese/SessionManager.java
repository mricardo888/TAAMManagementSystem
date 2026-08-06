package com.golden.geese;

/**
 * Manages the application's current user session using the Singleton pattern.
 *
 * <p>Only one {@code SessionManager} instance can exist during the
 * application's lifetime. The manager stores the currently signed-in user and
 * provides methods for accessing, updating, and ending the session.</p>
 */
public class SessionManager implements Session {
    private static volatile SessionManager instance;
    private User currentUser;

    /**
     * Prevents other classes from directly creating a
     * {@code SessionManager}.
     */
    private SessionManager() {}

    /**
     * Returns the shared {@code SessionManager} instance.
     *
     * @return the single shared {@code SessionManager} instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the user currently associated with this session.
     *
     * @return the current user, or {@code null} when no user is signed in
     */
    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the user currently associated with this session.
     *
     * @param user the user to store as the current user
     */
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Ends the current session and removes the signed-in user.
     */
    @Override
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Indicates whether a user is currently signed in.
     *
     * @return {@code true} when the session contains a current user;
     *         otherwise {@code false}
     */
    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
