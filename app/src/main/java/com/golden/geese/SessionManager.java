package com.golden.geese;

public class SessionManager implements Session {
    private static volatile SessionManager instance;
    private User currentUser;

    private SessionManager() {}

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

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
