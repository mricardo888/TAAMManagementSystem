package com.golden.geese.presenter;

import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.AuthView;

/**
 * Presenter for the multi-step sign-up flow, following the
 * Model-View-Presenter pattern. Implemented as a singleton so sign-up
 * state (username, email, password) persists across the individual steps
 * of the flow, then resets once sign-up completes or is abandoned.
 */
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

    /**
     * Returns the current sign-up presenter instance, creating one if none
     * exists. The same instance is reused across all steps of the sign-up
     * flow until {@link #completeProcess()} clears it.
     *
     * @return the singleton {@code SignUpPresenter} instance
     */
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

    /**
     * Validates the entered username and, if valid, stores it and advances
     * the view to the next step. Does nothing if the view hasn't been set.
     *
     * @param username the username entered by the user
     */
    public void validateName (String username) {
        if (view == null) {
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            view.showError("Username cannot be empty.");
            return;
        }
        setUsername(username);
        view.nextStep();
    }

    /**
     * Validates the entered email and, if valid, stores it and advances
     * the view to the next step. Does nothing if the view hasn't been set.
     *
     * @param email the email address entered by the user
     */
    public void validateEmail (String email) {
        if (view == null) {
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            view.showError("Email cannot be empty.");
            return;
        }
        setEmail(email);
        view.nextStep();
    }

    /**
     * Validates the entered password and its confirmation, then triggers
     * sign-up if both are valid. Does nothing if the view hasn't been set.
     *
     * @param password             the password entered by the user
     * @param passwordConfirmation the confirmation value, which must match password
     */

    public void validatePassword (String password, String passwordConfirmation) {
        if (view == null) {
            return;
        }
        if (password == null || password.length() < 6) {
            view.showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(passwordConfirmation)) {
            view.showError("Passwords do not match.");
            return;
        }
        setPassword(password);
        signup();
    }

    /**
     * Submits the collected username, email, and password to the attached
     * {@link AuthRepository}. On success, stores the signed-up
     * {@link User} as the current session user, advances the view, and
     * clears the singleton instance. On failure, reports the error to the view.
     */
    private void signup() {
        if (isBlank(username) || isBlank(email)) {
            view.showError("Sign-up details are incomplete. Please start again.");
            return;
        }

        view.showLoading();

        repo.signUp(email, username, password, new AuthCallBack() {
            @Override
            public void onSuccess(User user) {
                SessionManager.getInstance().setCurrentUser(user);
                view.nextStep();
                completeProcess();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    /**
     * Checks whether a value is {@code null} or contains only whitespace.
     *
     * @param value the value to check
     * @return {@code true} if the value is {@code null} or blank
     */
    private boolean isBlank (String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Detaches this presenter from its view if the given caller is the
     * currently attached view, preventing further updates to a view that
     * is being destroyed. Does nothing if {@code caller} does not match
     * the currently attached view.
     *
     * @param caller the view requesting detachment
     */
    public void onDestroy (AuthView caller) {
        if (caller == view) {
            view = null;
        }
    }

    public void completeProcess () {
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