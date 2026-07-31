package com.golden.geese.presenter;

import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.AuthView;

public class SignUpPresenter {

    private static SignUpPresenter instance;
    private AuthView view;
    private AuthRepository repo;

    private String username, email, password;
    private SignUpPresenter () {
        this.username = "";
        this.email = "";
        this.password = "";
    }

    public static SignUpPresenter getSignUpPresenter () {
        if (instance == null ) {
            instance = new SignUpPresenter();
            return instance;
        }
        return instance;
    }

    public void setView (AuthView view) {
        this.view = view;
    }

    public void setRepo (AuthRepository repo) {
        this.repo = repo;
    }

    public void validateName (String username) {
        if (username == null || username.trim().isEmpty()) {
            view.showError("Username cannot be empty.");
            return;
        }
        setUsername(username);
        view.nextStep();
    }

    public void validateEmail (String email) {
        if (email == null || email.trim().isEmpty()) {
            view.showError("Email cannot be empty.");
            return;
        }
        setEmail(email);
        view.nextStep();
    }

    public void validatePassword (String password, String passwordConfirmation) {
        if (password == null || password.length() < 6) {
            view.showError("Password must be at least 6 characters.");
            return;
        }
        setPassword(password);
        signup();
    }

    private void signup() {
        view.showLoading();

//        repo.signUp(email, username, password, new AuthCallBack() {
//            @Override
//            public void onSuccess(User user) {
//                view.goToHome();
//                completeProcess();
//            }
//
//            @Override
//            public void onError(String message) {
//                view.showError(message);
//            }
//        });
    }

    public void onDestroy (AuthView caller) {
        if (caller == view) {
            view = null;
        }
    }

    public void completeProcess () {
        // Need to call once user has created account and signed in
        // So that presenter memory and all data from signup process is cleaned
        instance = null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername (String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getEmail () {
        return email;
    }
}