package com.golden.geese.view;

/**
 * View contract for the login screen, implemented by specific UI
 */
public interface LoginView {

    /**
     * Navigates away from the login screen to the home screen.
     */
    void goToHome();

    /**
     * Displays a validation or sign-in error related to the email field.
     *
     * @param message a description of the error
     */
    void showEmailError(String message);

    /**
     * Displays a validation or sign-in error related to the password
     * field.
     *
     * @param message a description of the error
     */
    void showPasswordError(String message);

}