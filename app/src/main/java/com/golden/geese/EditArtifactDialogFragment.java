package com.golden.geese;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

/**
 * Dialogue for editing an artifact after it has been created.
 */
public class EditArtifactDialogFragment extends ArtifactDialogFragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();
    private Artifact artifact;

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    /**
     * Sets lot number to not be editable when editing an artifact.
     * @return false
     */
    @Override
    protected boolean isLotNumberEditable() {
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button saveButton = view.findViewById(R.id.save_button);

        CheckBox onDisplayCheckbox = view.findViewById(R.id.cb_on_display);

        if (artifact == null) {
            dismiss();
            return;
        }

        fillFields(artifact);

        // Load the current "On Display" status from the artifact
        if (onDisplayCheckbox != null) {
            onDisplayCheckbox.setChecked(artifact.isOnDisplay());
        }

        cancelButton.setOnClickListener(clickedView -> dismiss());
        saveButton.setOnClickListener(clickedView -> {
            if (!validateFields()) {
                return;
            }

            saveInputs(artifact);

            // Save the CheckBox state to the artifact
            if (onDisplayCheckbox != null) {
                artifact.setOnDisplay(onDisplayCheckbox.isChecked());
            }

            String previousImageUrl = artifact.getImage();
            boolean replacingImage = selectedImageUri != null;

            resolveImage(previousImageUrl, String.valueOf(artifact.getLotNum()), imageUrl -> {
                artifact.setImage(imageUrl);
                updateArtifact(replacingImage ? previousImageUrl : null);
            });
        });
    }

    /**
     * Updates the artifact database with the new artifact details.
     * @param oldImageUrlToDelete a string of the old image of the artifact if a new image has been set.
     */
    private void updateArtifact(String oldImageUrlToDelete)
    {
        repository.updateArtifact(
                artifact,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!isAdded()) {
                            return;
                        }

                        if (oldImageUrlToDelete != null && !oldImageUrlToDelete.isEmpty()) {
                            deleteCloudImage(oldImageUrlToDelete);
                        }

                        notifyArtifactSaved(artifact);

                        Toast.makeText(
                                requireContext(),
                                "Artifact updated",
                                Toast.LENGTH_SHORT
                        ).show();

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

    /**
     * Gets the title of the artifact editing form.
     * @return The string "Edit Artifact"
     */
    protected String getFormTitle() {
        return "Edit Artifact";
    }
}
