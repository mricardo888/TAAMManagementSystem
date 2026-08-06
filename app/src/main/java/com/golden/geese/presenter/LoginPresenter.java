package com.golden.geese.presenter;

import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.LoginView;

/**
 * Presenter for the login screen, following the Model-View-Presenter pattern.
 * Validates user-entered credentials, delegates authentication to an
 * {@link AuthRepository}, and updates the bound {@link LoginView} based on
 * the result.
 */
public class LoginPresenter {

    private LoginView view;
    private AuthRepository repo;

/**
 * Constructor; Creates a presenter with no view or repository attached.
 * */
    public LoginPresenter() {
    }

    /**
     * Attaches the view this presenter will report validation errors and
     * navigation events to.
     *
     * @param view the login view to bind, or {@code null} to detach
     */
    public void setView (LoginView view) {
        this.view = view;
    }


    /**
     * Attaches the repository used to perform authentication.
     *
     * @param repo the authentication repository to use
     */
    public void setRepo (AuthRepository repo) {
        this.repo = repo;
    }

    /**
     * Validates the email and password, then attempts to sign the user in
     * through the attached {@link AuthRepository}.
     * <p>
     * Does nothing if the view or repository hasn't been set. Otherwise,
     * checks for empty fields first and reports any error to the view
     * without making a network call. On success, stores the signed-in
     * {@link User} as the current session user and navigates to home. On
     * failure, reports the error to the view.
     *
     * @param email    the email address entered by the user
     * @param password the password entered by the user
     */
    public void login(String email, String password) {
        if (view == null || repo == null) {
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            view.showEmailError("Email cannot be empty.");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
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
                view.showEmailError(message);
                view.showPasswordError(message);
            }
        });
    }

/**
 * Detaches this presenter from its view and repository if the given
 * caller is the currently attached view, preventing further updates to
 * a view that is being destroyed.
 *
 * @param caller the view requesting detachment
 * */
    public void onDestroy(LoginView caller) {
        if (caller == view) {
            view = null;
            repo = null;
        }
    }
}