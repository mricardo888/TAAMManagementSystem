package com.golden.geese;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;

import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

public class EditArtifactDialogFragment extends DialogFragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();
    private Artifact artifact;
    private OnArtifactUpdatedListener updatedListener;

    private EditText nameInput;
    private EditText descriptionInput;
    private EditText categoryInput;
    private EditText materialInput;
    private EditText dynastyInput;

    public interface OnArtifactUpdatedListener
    {
        void onArtifactUpdated(Artifact artifact);
    }

    public void setArtifact(Artifact artifact, OnArtifactUpdatedListener listener)
    {
        this.artifact = artifact;
        this.updatedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_artifact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.edit_name);
        descriptionInput = view.findViewById(R.id.edit_description);
        categoryInput = view.findViewById(R.id.edit_category);
        materialInput = view.findViewById(R.id.edit_material);
        dynastyInput = view.findViewById(R.id.edit_dynasty);

        Button cancelButton = view.findViewById(R.id.cancel_edit_button);
        Button saveButton = view.findViewById(R.id.save_artifact_button);

        if (artifact == null) {
            dismiss();
            return;
        }

        fillFields();

        cancelButton.setOnClickListener(clickedView -> dismiss());
        saveButton.setOnClickListener(clickedView -> saveChanges());
    }

    private void fillFields()
    {
        nameInput.setText(artifact.getName());
        descriptionInput.setText(artifact.getDescription());
        categoryInput.setText(artifact.getCategory());
        dynastyInput.setText(artifact.getDynasty());
        materialInput.setText(artifact.getMaterial());
    }

    private void saveChanges()
    {
        String name = nameInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }

        artifact.setName(name);
        artifact.setDescription(descriptionInput.getText().toString().trim());
        artifact.setCategory(categoryInput.getText().toString().trim());
        artifact.setDynasty(dynastyInput.getText().toString().trim());
        artifact.setMaterial(materialInput.getText().toString().trim());

        saveArtifactToBackend();
    }

    private void saveArtifactToBackend()
    {
        repository.updateArtifact(
                artifact,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!isAdded()) {
                            return;
                        }

                        if (updatedListener != null) {
                            updatedListener.onArtifactUpdated(artifact);
                        }

                        Toast.makeText(
                                requireContext(),
                                "Artifact updated",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                message == null
                                        ? "Failed to update artifact"
                                        : message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
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
