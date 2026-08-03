package com.golden.geese;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

public class EditArtifactDialogFragment extends ArtifactDialogFragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();
    private Artifact artifact;

    public void setArtifact(Artifact artifact)
    {
        this.artifact = artifact;
    }

    @Override
    protected boolean isLotNumberEditable() {
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button saveButton = view.findViewById(R.id.save_button);

        if (artifact == null) {
            dismiss();
            return;
        }

        fillFields(artifact);

        cancelButton.setOnClickListener(clickedView -> dismiss());
        saveButton.setOnClickListener(clickedView -> {
            if (!validateFields()) {
                return;
            }

            saveInputs(artifact);

            String previousImageUrl = artifact.getImage();
            boolean replacingImage = selectedImageUri != null;

            resolveImage(previousImageUrl, String.valueOf(artifact.getLotNum()), imageUrl -> {
                artifact.setImage(imageUrl);
                updateArtifact(replacingImage ? previousImageUrl : null);
            });
        });
    }

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

    protected String getFormTitle() {
        return "Edit Artifact";
    }
}
