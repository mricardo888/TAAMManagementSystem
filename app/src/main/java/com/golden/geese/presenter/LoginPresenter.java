package com.golden.geese.presenter;

import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.AuthView;

public class LoginPresenter {

    private AuthView view;
    private AuthRepository repo;

    public LoginPresenter(AuthView view, AuthRepository repo) {
        this.view = view;
        this.repo = repo;
    }

    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            view.showError("Email cannot be empty.");
            return;
        }
        if (password == null || password.isEmpty()) {
            view.showError("Password cannot be empty.");
            return;
        }

        view.showLoading();

//        repo.signIn(email, password, new AuthCallBack() {
//            @Override
//            public void onSuccess(User user) {
//                view.goToHome();
//            }
//
//            @Override
//            public void onError(String message) {
//                view.showError(message);
//            }
//        });
    }

    public void goToSignUp() {
        // Activity that implements AuthView, triggered from here if needed.
    }
}