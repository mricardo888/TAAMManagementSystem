package com.golden.geese.presenter;

import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.ui.LoginFragment;
import com.golden.geese.view.AuthView;
import com.golden.geese.view.LoginView;

public class LoginPresenter {

    private LoginView view;
    private AuthRepository repo;

    public LoginPresenter() {
    }

    public void setView (LoginView view) {
        this.view = view;
    }

    public void setRepo (AuthRepository repo) {
        this.repo = repo;
    }

    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            view.showEmailError("Email cannot be empty.");
            return;
        }
        if (password == null || password.isEmpty()) {
            view.showPasswordError("Password cannot be empty.");
            return;
        }

        repo.signIn(email, password, new AuthCallBack() {
            @Override
            public void onSuccess(User user) {
                SessionManager.getInstance().setCurrentUser(user);
                view.goToHome();
            }

            @Override
            public void onError(String message) {
                view.showPasswordError(message);
            }
        });
    }

    public void onDestroy(LoginView caller) {
        if (caller == view) {
            view = null;
            repo = null;
        }
    }
}