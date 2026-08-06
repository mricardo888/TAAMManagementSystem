package com.golden.geese.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.R;

/**
 * Just the welcome screen that is displayed when user is not logged in. Prompts two buttons for
 * user if they want to sign in or create and account.
 */
public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_welcome_screen, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonSignUp = view.findViewById(R.id.SignUpButton);
        Button buttonLogIn = view.findViewById(R.id.LoginButton);

        buttonSignUp.setOnClickListener(v -> {loadFragment(new SignUpNameFragment());});
        buttonLogIn.setOnClickListener(v -> {loadFragment(new LoginFragment());});


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
}