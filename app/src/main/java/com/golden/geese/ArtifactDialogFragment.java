package com.golden.geese;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public abstract class ArtifactDialogFragment extends DialogFragment {
    protected EditText nameInput;
    protected EditText descriptionInput;
    protected EditText categoryInput;
    protected EditText materialInput;
    protected EditText dynastyInput;

    protected OnArtifactSavedListener savedListener;

    public interface OnArtifactSavedListener {
        void onArtifactSaved(Artifact artifact);
    }

    public void setOnArtifactSavedListener(OnArtifactSavedListener listener) {
        this.savedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_artifact_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.artifact_name);
        descriptionInput = view.findViewById(R.id.artifact_description);
        categoryInput = view.findViewById(R.id.artifact_category);
        materialInput = view.findViewById(R.id.artifact_material);
        dynastyInput = view.findViewById(R.id.artifact_dynasty);
    }

    protected boolean validateFields() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String material = materialInput.getText().toString().trim();
        String dynasty = dynastyInput.getText().toString().trim();

        boolean valid = true;

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            valid = false;
        } else {
            nameInput.setError(null);
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            valid = false;
        } else {
            descriptionInput.setError(null);
        }

        if (category.isEmpty()) {
            categoryInput.setError("Category is required");
            valid = false;
        } else {
            categoryInput.setError(null);
        }

        if (material.isEmpty()) {
            materialInput.setError("Material is required");
            valid = false;
        } else {
            materialInput.setError(null);
        }

        if (dynasty.isEmpty()) {
            dynastyInput.setError("Dynasty is required");
            valid = false;
        } else {
            dynastyInput.setError(null);
        }

        if (!valid) {
            focusInvalidField();
        }

        return valid;
    }

    private void focusInvalidField() {
        if (nameInput.getError() != null) {
            nameInput.requestFocus();
        } else if (descriptionInput.getError() != null) {
            descriptionInput.requestFocus();
        } else if (categoryInput.getError() != null) {
            categoryInput.requestFocus();
        } else if (materialInput.getError() != null) {
            materialInput.requestFocus();
        } else if (dynastyInput.getError() != null) {
            dynastyInput.requestFocus();
        }
    }

    protected void fillFields(@NonNull Artifact artifact)
    {
        nameInput.setText(artifact.getName());
        descriptionInput.setText(artifact.getDescription());
        categoryInput.setText(artifact.getCategory());
        dynastyInput.setText(artifact.getDynasty());
        materialInput.setText(artifact.getMaterial());
    }

    protected void saveInputs(@NonNull Artifact artifact) {
        artifact.setName(nameInput.getText().toString().trim());
        artifact.setDescription(descriptionInput.getText().toString().trim());
        artifact.setCategory(categoryInput.getText().toString().trim());
        artifact.setDynasty(dynastyInput.getText().toString().trim());
        artifact.setMaterial(materialInput.getText().toString().trim());
    }

    protected void notifyArtifactSaved(@NonNull Artifact artifact) {
        if (savedListener != null)
        {
            savedListener.onArtifactSaved(artifact);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog == null) {
            return;
        }

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            window.setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.92
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setDimAmount(0.55f);
        }
    }
}
