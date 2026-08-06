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
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.R;
import com.golden.geese.model.FirebaseAuthRepository;
import com.golden.geese.presenter.SignUpPresenter;
import com.golden.geese.view.AuthView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;


/**
 * This is the email fragment view for the 3-step sign-up process. It contains its own signUpPresenter
 * which handles logics and repo/model authentication. This fragment handles getting the email and
 * input from the UI and passes it onto the presenter and will update the UI based the presenter.
 */

public class SignUpEmailFragment extends Fragment implements AuthView {

    private SignUpPresenter presenter;
    private TextInputLayout inputLayout;
    private TextInputEditText emailInput;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup_email, container, false);
        presenter = SignUpPresenter.getSignUpPresenter();
        presenter.setRepo(new FirebaseAuthRepository());
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputLayout = view.findViewById(R.id.emailInputLayout);
        emailInput = view.findViewById(R.id.emailInput);
        nextButton = view.findViewById(R.id.nextButton);
        ImageButton backButton = view.findViewById(R.id.backButton);

        // Set the text back to original
        emailInput.setText(presenter.getEmail());
        presenter.setView(this);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {
            nextButton.setClickable(false);
            presenter.validateEmail(
                Objects.requireNonNull(emailInput.getText()).toString());});
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
     * setting the next button to clickable. Overridden as method is from interface
     * @param message a human-readable description of the error
     */
    @Override
    public void showError(String message) {
        nextButton.setClickable(true);
        inputLayout.setError(message);
        emailInput.setError(message);
    }

    /**
     * Shows loading state, overridden as it is from interface
     */
    @Override
    public void showLoading() {

    }

    /**
     * Changes screen to next step in sign-up process, overridden as it is from interface
     */
    @Override
    public void nextStep() {
        loadFragment(new SignUpPasswordFragment());
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