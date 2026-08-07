package com.golden.geese.model;

/** Manages the /users profile records that back each authenticated account */
public interface UserProfileRepository {
    /** Creates the profile record for a newly signed-up user */
    void createUserProfile(String uid, String username, String email, RepositoryCallback<Void> callback);

    /** Resolves whether the given user has admin privileges */
    void isAdmin(String uid, RepositoryCallback<Boolean> callback);

    /** Fetches the display username for the given user */
    void getUsername(String uid, RepositoryCallback<String> callback);

    /** Updates the display username for the given user */
    void updateUsername(String uid, String username, RepositoryCallback<Void> callback);
}
