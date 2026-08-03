package com.golden.geese.model;

import com.golden.geese.User;

public interface AuthRepository {
    void signIn(String email, String pwd, AuthCallBack callback);

    void signUp(String email, String username, String pwd, AuthCallBack callback);

    User getCurrentUser();

    void signOut();

    void reauthenticate(String currentPassword, RepositoryCallback<Void> callback);

    void updateEmail(String newEmail, RepositoryCallback<Void> callback);

    void updatePassword(String newPassword, RepositoryCallback<Void> callback);
}
