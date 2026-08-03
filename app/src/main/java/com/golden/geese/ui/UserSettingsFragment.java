package com.golden.geese.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.golden.geese.R;
import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.model.FirebaseAuthRepository;
import com.golden.geese.model.RepositoryCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class UserSettingsFragment extends Fragment {

    private final AuthRepository authRepository = new FirebaseAuthRepository();

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

        // TODO: load real username once profile data is exposed through a repository
        settingsNameInput.setText("Jane Doe");

        User currentUser = SessionManager.getInstance().getCurrentUser();
        settingsEmailInput.setText(currentUser == null ? "" : currentUser.getEmail());

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

        if (newName.isEmpty()) {
            settingsNameInputLayout.setError("New username cannot be empty");
            return;
        }
        settingsNameInputLayout.setError(null);

        if (newEmail.isEmpty()) {
            settingsEmailInputLayout.setError("New email cannot be empty");
            return;
        }
        settingsEmailInputLayout.setError(null);

        String currentPassword = Objects.requireNonNull(settingsCurrentPasswordInput.getText()).toString();
        String newPassword = Objects.requireNonNull(settingsNewPasswordInput.getText()).toString();
        String confirmNewPassword = Objects.requireNonNull(settingsConfirmNewPasswordInput.getText()).toString();

        boolean emailChanged = !newEmail.equals(originalEmail);
        boolean changingPassword = !newPassword.isEmpty() || !confirmNewPassword.isEmpty();

        if (changingPassword) {
            if (newPassword.length() < 6) {
                settingsNewPasswordInputLayout.setError("New password must be at least 6 characters");
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                settingsConfirmNewPasswordInputLayout.setError("Passwords do not match");
                return;
            }
        }
        settingsNewPasswordInputLayout.setError(null);
        settingsConfirmNewPasswordInputLayout.setError(null);

        if (!emailChanged && !changingPassword) {
            exitEditMode();
            return;
        }

        if (currentPassword.isEmpty()) {
            settingsCurrentPasswordInputLayout.setError("Enter your current password to confirm these changes");
            return;
        }
        settingsCurrentPasswordInputLayout.setError(null);

        settingsSaveButton.setEnabled(false);
        authRepository.reauthenticate(currentPassword, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) {
                    return;
                }
                applyAccountChanges(emailChanged, newEmail, changingPassword, newPassword);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                settingsSaveButton.setEnabled(true);
                settingsCurrentPasswordInputLayout.setError("Current password is incorrect");
            }
        });
    }

    private void applyAccountChanges(boolean emailChanged, String newEmail, boolean changingPassword, String newPassword) {
        if (!emailChanged) {
            applyPasswordChange(newPassword);
            return;
        }

        authRepository.updateEmail(newEmail, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(
                        requireContext(),
                        "Verification email sent to " + newEmail + ". Confirm it to finish updating your email.",
                        Toast.LENGTH_LONG
                ).show();

                if (changingPassword) {
                    applyPasswordChange(newPassword);
                } else {
                    settingsSaveButton.setEnabled(true);
                    exitEditMode();
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                settingsSaveButton.setEnabled(true);
                settingsEmailInputLayout.setError(message == null ? "Could not update email" : message);
            }
        });
    }

    private void applyPasswordChange(String newPassword) {
        authRepository.updatePassword(newPassword, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) {
                    return;
                }
                settingsSaveButton.setEnabled(true);
                Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show();
                exitEditMode();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                settingsSaveButton.setEnabled(true);
                settingsNewPasswordInputLayout.setError(message == null ? "Could not update password" : message);
            }
        });
    }

    private void cancelEdit() {
        settingsNameInput.setText(originalName);
        settingsEmailInput.setText(originalEmail);
        exitEditMode();
    }

    private void logout() {
        authRepository.signOut();
        SessionManager.getInstance().logout();

        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, new WelcomeFragment());
        transaction.commit();
    }
}