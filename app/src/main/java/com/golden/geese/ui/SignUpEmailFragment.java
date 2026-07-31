package com.golden.geese.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import androidx.lifecycle.ViewModelProvider;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.R;
import com.golden.geese.presenter.SignUpPresenter;
import com.golden.geese.view.AuthView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SignUpEmailFragment extends Fragment implements AuthView {

    private SignUpPresenter presenter;
    private TextInputLayout inputLayout;
    private TextInputEditText emailInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup_email, container, false);
        presenter = SignUpPresenter.getSignUpPresenter();
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputLayout = view.findViewById(R.id.emailInputLayout);
        emailInput = view.findViewById(R.id.emailInput);
        Button nextButton = view.findViewById(R.id.nextButton);
        ImageButton backButton = view.findViewById(R.id.backButton);

        // Set the text back to original
        emailInput.setText(presenter.getEmail());
        presenter.setView(this);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {presenter.validateEmail(
                Objects.requireNonNull(emailInput.getText()).toString());});
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void showError(String message) {
        inputLayout.setError(message);
        emailInput.setError(message);
    }

    @Override
    public void showLoading() {

    }
    @Override
    public void goToHome() {

    }
    @Override
    public void nextStep() {
        loadFragment(new SignUpPasswordFragment());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy(this);
    }
}