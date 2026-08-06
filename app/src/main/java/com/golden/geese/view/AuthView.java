package com.golden.geese.view;

/**
 * View contract for the sign-up flow, implemented by whatever UI drives
 * the multi-step sign-up screens (e.g. an Activity or Fragment).
 */
public interface AuthView {
    /**
     * Displays an error message to the user
     *
     * @param message a human-readable description of the error
     */
    void showError(String message);
    /**
     * Displays a loading indicator while a sign-up request is in progress.
     */
    void showLoading();
    /**
     * Advances the view to the next step of the sign-up flow.
     */
    void nextStep();
}