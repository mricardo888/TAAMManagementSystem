package com.golden.geese.model;

/** Manages sign-in, sign-up, and account maintenance for the app's authenticated user */
public interface AuthRepository {
    /** Signs in with email/password and resolves the callback with the loaded User */
    void signIn(String email, String pwd, AuthCallBack callback);

    /** Creates a new account and profile, then resolves the callback with the new user */
    void signUp(String email, String username, String pwd, AuthCallBack callback);

    /** Resolves the callback with the currently signed-in user, or an error if none is signed in */
    void getCurrentUser(AuthCallBack callback);

    /** Signs the current user out */
    void signOut();

    /** Re-authenticates the current user with their current password, required before sensitive updates */
    void reauthenticate(String currentPassword, RepositoryCallback<Void> callback);

    /** Sends a verification email to update the current user's email address */
    void updateEmail(String newEmail, RepositoryCallback<Void> callback);

    /** Updates the current user's password */
    void updatePassword(String newPassword, RepositoryCallback<Void> callback);
}
