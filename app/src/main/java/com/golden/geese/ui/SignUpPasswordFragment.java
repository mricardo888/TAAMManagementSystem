package com.golden.geese.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.HomeFragment;
import com.golden.geese.R;
import com.golden.geese.model.FirebaseAuthRepository;
import com.golden.geese.presenter.SignUpPresenter;
import com.golden.geese.view.AuthView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;


/**
 * This is the password fragment view for the 3-step sign-up process. It contains its own signUpPresenter
 * which handles logics and repo/model authentication. This fragment handles getting the password and
 * input from the UI and passes it onto the presenter and will update the UI based the presenter.
 */

public class SignUpPasswordFragment extends Fragment implements AuthView {

    private SignUpPresenter presenter;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordInput;
    private TextInputLayout passwordConfirmationInputLayout;
    private TextInputEditText passwordConfirmationInput;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup_password, container, false);
        presenter = SignUpPresenter.getSignUpPresenter();
        presenter.setRepo(new FirebaseAuthRepository());
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        passwordInput = view.findViewById(R.id.passwordInput);
        passwordConfirmationInput = view.findViewById(R.id.passwordConfirmationInput);
        passwordConfirmationInputLayout = view.findViewById(R.id.passwordConfirmationInputLayout);
        nextButton = view.findViewById(R.id.nextButton);
        ImageButton backButton = view.findViewById(R.id.backButton);

        // Set the text back to original
        passwordInput.setText(presenter.getPassword());
        presenter.setView(this);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {
            nextButton.setClickable(false);
            presenter.validatePassword(Objects.requireNonNull(passwordInput.getText()).toString(),
                    Objects.requireNonNull(passwordConfirmationInput.getText()).toString());
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
     * Sets error state on password input and displays error message while also
     * setting the next button to clickable. Overridden as method is from interface
     * @param message a human-readable description of the error
     */
    @Override
    public void showError(String message) {
        nextButton.setClickable(true);
        passwordInputLayout.setError(message);
        passwordInput.setError(message);
        passwordConfirmationInput.setError(message);
        passwordConfirmationInputLayout.setError(message);
        passwordConfirmationInput.setText("");
    }

    /**
     * Shows loading state, overridden as it is from interface
     */
    @Override
    public void showLoading() {

    }

    /**
     * Changes screen to next step in sign-up process, overridden as it is from interface.
     * In this case the next step would be finishing the sign-up process and loading the home
     * fragment
     */
    public void nextStep() {
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