package com.golden.geese;

public interface Session {
    User getCurrentUser();
    void setCurrentUser(User user);
    void logout();
    boolean isLoggedIn();
}
