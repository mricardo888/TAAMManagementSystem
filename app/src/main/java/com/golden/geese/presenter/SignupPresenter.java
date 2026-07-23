package com.golden.geese.presenter;

import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.AuthView;

public class SignUpPresenter {


    private AuthView view;
    private AuthRepository repo;

    public SignUpPresenter(AuthView view, AuthRepository repo) {
        this.view = view;
        this.repo = repo;
    }

    public void signup(String email, String username, String password) {
        if (email == null || email.trim().isEmpty()) {
            view.showError("Email cannot be empty.");
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            view.showError("Username cannot be empty.");
            return;
        }
        if (password == null || password.length() < 6) {
            view.showError("Password must be at least 6 characters.");
            return;
        }

        view.showLoading();

        repo.signUp(email, username, password, new AuthCallBack() {
            @Override
            public void onSuccess(User user) {
                SessionManager.getInstance().setCurrentUser(user);
                view.goToHome();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}