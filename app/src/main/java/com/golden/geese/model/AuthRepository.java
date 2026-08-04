package com.golden.geese.model;

public interface AuthRepository {
    void signIn(String email, String pwd, AuthCallBack callback);

    void signUp(String email, String username, String pwd, AuthCallBack callback);

    void getCurrentUser(AuthCallBack callback);

    void signOut();

    void reauthenticate(String currentPassword, RepositoryCallback<Void> callback);

    void updateEmail(String newEmail, RepositoryCallback<Void> callback);

    void updatePassword(String newPassword, RepositoryCallback<Void> callback);
}
