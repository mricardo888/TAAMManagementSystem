package com.golden.geese.model;

public interface UserProfileRepository {
    void createUserProfile(String uid, String username, String email, RepositoryCallback<Void> callback);

    void isAdmin(String uid, RepositoryCallback<Boolean> callback);

    void getUsername(String uid, RepositoryCallback<String> callback);
}
