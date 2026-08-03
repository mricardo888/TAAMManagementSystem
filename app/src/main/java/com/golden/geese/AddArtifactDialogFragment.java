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

            checkLotNumberThenCreate();
        });
    }

    private void checkLotNumberThenCreate() {
        int lotNum = getLotNumberValue();

        repository.doesLotNumberExist(lotNum, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!isAdded()) {
                    return;
                }

                if (exists) {
                    lotNumberInput.setError("An artifact with this lot number already exists");
                    lotNumberInput.requestFocus();
                    return;
                }

                createArtifact(lotNum);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        message == null ? "Could not verify lot number" : message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void createArtifact(int lotNum) {
        Artifact newArtifact = new Artifact();
        saveInputs(newArtifact);
        newArtifact.setLotNum(lotNum);

        resolveImage("", String.valueOf(lotNum), imageUrl -> {
            newArtifact.setImage(imageUrl == null ? "" : imageUrl);
            addArtifact(newArtifact);
        });
    }

    private void addArtifact(Artifact newArtifact)
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

    protected String getFormTitle() {
        return "Add Artifact";
    }
}
