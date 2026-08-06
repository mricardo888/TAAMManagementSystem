package com.golden.geese.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.HomeFragment;
import com.golden.geese.R;
import com.golden.geese.model.FirebaseAuthRepository;
import com.golden.geese.presenter.LoginPresenter;
import com.golden.geese.presenter.SignUpPresenter;
import com.golden.geese.view.LoginView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

/**
 * This is the login view fragment. It follows the MVP design format and has its own LoginPresenter
 * which communicates with repo/model and authenticates user. This view fragment is responsible for
 * getting input from the UI and passing it only to presenter and will update the UI based on the
 * presenter logic.
 */

public class LoginFragment extends Fragment implements LoginView{
    private LoginPresenter presenter;
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login_screen, container, false);
        presenter = new LoginPresenter();
        presenter.setRepo(new FirebaseAuthRepository());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailLayout = view.findViewById(R.id.LoginEmailInputLayout);
        emailInput = view.findViewById(R.id.LoginEmailInput);
        passwordLayout = view.findViewById(R.id.LoginPasswordInputLayout);
        passwordInput = view.findViewById(R.id.LoginPasswordInput);
        nextButton = view.findViewById(R.id.nextButton);
        ImageButton backButton = view.findViewById(R.id.backButton);
        TextView signUpButton = view.findViewById(R.id.signUpLink);

        presenter.setView(this);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {
            nextButton.setClickable(false);
            presenter.login(
                Objects.requireNonNull(emailInput.getText()).toString(),
                    Objects.requireNonNull(passwordInput.getText()).toString());

        });

        signUpButton.setOnClickListener(v -> {
            loadFragment(new SignUpNameFragment());
        });
    }

    /**
     * Loads a screen fragment into the fragment container
     * @param fragment the fragment to be loaded
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Sets error state on email input and displays error message while also
     * setting the login button to clickable. Overridden as method is from interface
     * @param message a human-readable description of the error
     */
    @Override
    public void showEmailError(String message) {
        nextButton.setClickable(true);
        emailLayout.setError(message);
        emailInput.setError(message);
    }

    /**
     * Sets error state on password input and displays error message while also
     * setting the login button to clickable. Overridden as method is from interface
     * @param message a human-readable description of the error
     */
    @Override
    public void showPasswordError(String message) {
        nextButton.setClickable(true);
        passwordLayout.setError(message);
        passwordInput.setError(message);
    }


    /**
     * Loads the home fragment, which the presenter calls once user login details are verified
     */
    @Override
    public void goToHome() {
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new HomeFragment());
        transaction.commit();
    }

    /**
     * Called when fragment is destroyed, overridden to include unlinking fragment from presenter
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy(this);
    }

}

