package com.golden.geese;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

public class AddArtifactDialogFragment extends ArtifactDialogFragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();
    private Artifact newArtifact;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button saveButton = view.findViewById(R.id.save_button);

        cancelButton.setOnClickListener(clickedView -> dismiss());
        saveButton.setOnClickListener(clickedView -> {
            if (!validateFields()) {
                return;
            }

            newArtifact = new Artifact();
            saveInputs(newArtifact);
            addArtifact();
        });
    }

    private void addArtifact()
    {
        repository.addArtifact(
                newArtifact,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!isAdded()) {
                            return;
                        }

                        notifyArtifactSaved(newArtifact);

                        Toast.makeText(
                                requireContext(),
                                "Artifact added",
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
                                        ? "Failed to add artifact"
                                        : message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
