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
public class LoginFragment extends Fragment implements LoginView{
    private LoginPresenter presenter;
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;

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
        Button nextButton = view.findViewById(R.id.nextButton);
        ImageButton backButton = view.findViewById(R.id.backButton);
        TextView signUpButton = view.findViewById(R.id.signUpLink);

        presenter.setView(this);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nextButton.setOnClickListener(v -> {
            presenter.login(
                Objects.requireNonNull(emailInput.getText()).toString(),
                    Objects.requireNonNull(passwordInput.getText()).toString());});

        signUpButton.setOnClickListener(v -> {
            loadFragment(new SignUpNameFragment());
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    @Override
    public void showEmailError(String message) {
        emailLayout.setError(message);
        emailInput.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        passwordLayout.setError(message);
        passwordInput.setError(message);
    }


    @Override
    public void goToHome() {
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new HomeFragment());
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy(this);
    }

}

