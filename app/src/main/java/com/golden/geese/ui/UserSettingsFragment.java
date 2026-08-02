package com.golden.geese.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class UserSettingsFragment extends Fragment {

    private TextInputEditText settingsNameInput;
    private TextInputEditText settingsEmailInput;
    private TextInputEditText settingsCurrentPasswordInput;
    private TextInputEditText settingsNewPasswordInput;
    private TextInputLayout settingsEmailInputLayout;
    private TextInputLayout settingsNameInputLayout;
    private TextInputEditText settingsConfirmNewPasswordInput;
    private TextInputLayout settingsCurrentPasswordInputLayout;
    private TextInputLayout settingsNewPasswordInputLayout;
    private TextInputLayout settingsConfirmNewPasswordInputLayout;
    private ImageButton settingsEditButton;
    private String originalName;
    private String originalEmail;
    private TextInputEditText settingsLockedPasswordInput;
    private TextInputLayout settingsLockedPasswordInputLayout;
    private Group settingsPasswordEditGroup;
    private Button settingsSaveButton;
    private Button settingsCancelButton;
    private MaterialButton settingsLogoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsNameInput = view.findViewById(R.id.settingsNameInput);
        settingsNameInputLayout = view.findViewById(R.id.settingsNameInputLayout);
        settingsEmailInput = view.findViewById(R.id.settingsEmailInput);
        settingsEmailInputLayout = view.findViewById(R.id.settingsEmailInputLayout);
        settingsCurrentPasswordInput = view.findViewById(R.id.settingsCurrentPasswordInput);
        settingsNewPasswordInput = view.findViewById(R.id.settingsNewPasswordInput);
        settingsConfirmNewPasswordInput = view.findViewById(R.id.settingsConfirmNewPasswordInput);
        settingsCurrentPasswordInputLayout = view.findViewById(R.id.settingsCurrentPasswordInputLayout);
        settingsNewPasswordInputLayout = view.findViewById(R.id.settingsNewPasswordInputLayout);
        settingsConfirmNewPasswordInputLayout = view.findViewById(R.id.settingsConfirmNewPasswordInputLayout);
        settingsEditButton = view.findViewById(R.id.settingsEditButton);
        settingsLockedPasswordInput = view.findViewById(R.id.settingsLockedPasswordInput);
        settingsLockedPasswordInputLayout = view.findViewById(R.id.settingsLockedPasswordInputLayout);
        settingsPasswordEditGroup = view.findViewById(R.id.settingsPasswordEditGroup);

        settingsSaveButton = view.findViewById(R.id.settingsSaveButton);
        settingsCancelButton = view.findViewById(R.id.settingsCancelButton);
        settingsLogoutButton = view.findViewById(R.id.settingsLogoutButton);
        ImageButton settingsBackButton = view.findViewById(R.id.settingsBackButton);

        // TODO: load real user data here instead of placeholders, prob get from repo
        settingsNameInput.setText("Jane Doe");
        settingsEmailInput.setText("jane@example.com");
        //

        settingsBackButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        settingsEditButton.setOnClickListener(v -> enterEditMode());
        settingsSaveButton.setOnClickListener(v -> saveChanges());
        settingsCancelButton.setOnClickListener(v -> cancelEdit());
        settingsLogoutButton.setOnClickListener(v -> logout());
    }

    private void enterEditMode() {
        originalName = settingsNameInput.getText().toString();
        originalEmail = settingsEmailInput.getText().toString();

        settingsNameInput.setEnabled(true);
        settingsEmailInput.setEnabled(true);

        settingsLogoutButton.setVisibility(View.GONE);
        settingsCancelButton.setVisibility(View.VISIBLE);
        settingsSaveButton.setVisibility(View.VISIBLE);

        settingsLockedPasswordInputLayout.setVisibility(View.GONE);
        settingsPasswordEditGroup.setVisibility(View.VISIBLE);

        settingsEditButton.setVisibility(View.GONE);
    }

    private void exitEditMode() {
        settingsNameInput.setEnabled(false);
        settingsEmailInput.setEnabled(false);


        settingsLogoutButton.setVisibility(View.VISIBLE);
        settingsCancelButton.setVisibility(View.GONE);
        settingsSaveButton.setVisibility(View.GONE);

        settingsPasswordEditGroup.setVisibility(View.GONE);
        clearPasswordFields();
        settingsLockedPasswordInputLayout.setVisibility(View.VISIBLE);

        settingsEditButton.setVisibility(View.VISIBLE);
    }

    private void clearPasswordFields() {
        settingsCurrentPasswordInput.setText("");
        settingsNewPasswordInput.setText("");
        settingsConfirmNewPasswordInput.setText("");
        settingsCurrentPasswordInputLayout.setError(null);
        settingsNewPasswordInputLayout.setError(null);
        settingsConfirmNewPasswordInputLayout.setError(null);
    }

    private void saveChanges() {
        String newName = Objects.requireNonNull(settingsNameInput.getText()).toString().trim();
        String newEmail = Objects.requireNonNull(settingsEmailInput.getText()).toString().trim();

        boolean nameReady = false , emailReady = false, passwordReady = false;

        if(!newName.equals("")) { // TODO: check if newName is diff as current name
            if (newName.isEmpty()) {
                settingsNameInput.setError("New username cannot be empty");
                settingsNameInputLayout.setError("New username cannot be empty");
            } else {
                nameReady = true;
                // TODO: let repo change username
            }
        } else {
            nameReady = true;
        }

        if(!newEmail.equals("")) { // TODO: check if newEmail is diff as current email
            if (newEmail.isEmpty()) {
                settingsEmailInput.setError("New username cannot be empty");
                settingsEmailInputLayout.setError("New username cannot be empty");
            } else {
                emailReady = true;
                // TODO: let repo change email
            }
        } else {
            emailReady = true;
        }


        String currentPassword = Objects.requireNonNull(settingsCurrentPasswordInput.getText()).toString();
        String newPassword = Objects.requireNonNull(settingsNewPasswordInput.getText()).toString();
        String confirmNewPassword = Objects.requireNonNull(settingsConfirmNewPasswordInput.getText()).toString();

        boolean changingPassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty();

        if (changingPassword) {
            if (currentPassword.isEmpty()) {
                settingsCurrentPasswordInputLayout.setError("Enter your current password");
            }
            else if(!currentPassword.equals(null)) { // TODO: make sure current password is correct
                settingsCurrentPasswordInputLayout.setError("Current password is incorrect");
            }
            else if (newPassword.length() < 6) {
                settingsNewPasswordInputLayout.setError("New password must be at least 6 characters");
            }
            else if (!newPassword.equals(confirmNewPassword)) {
                settingsConfirmNewPasswordInputLayout.setError("Passwords do not match");
            }
            else {
                passwordReady = true;
                // TODO: call repo to update the user's password for newPassword
            }
        }
        if (nameReady && emailReady && passwordReady) {
            exitEditMode();
        }
    }

    private void cancelEdit() {
        settingsNameInput.setText(originalName);
        settingsEmailInput.setText(originalEmail);
        exitEditMode();
    }

    private void logout() {

        // TODO: clear session/auth token via your AuthRepository

        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new WelcomeFragment());
        transaction.commit();
    }
}