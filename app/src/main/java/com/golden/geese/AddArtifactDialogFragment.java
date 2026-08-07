/*
 * AddArtifactDialogFragment
 * Daniel Wang
 * Add Artifact Fragment
 *
 * This code is provided as part of the coursework for CSCB07H3
 * at the University of Toronto.
 *
 * Unauthorized reproduction, distribution, or sharing of this code is strictly
 * prohibited and constitutes a violation of the University of
 * Toronto Code of Behaviour on Academic Matters.
 *
 */
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

    /**
     * onViewCreated
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
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

    /**
     * CheckLotNumberThenCreate checks if a lot number exists, returns null if so, otherwise, it
     * creates the artifact
     */
    private void checkLotNumberThenCreate() {
        int lotNum = getLotNumberValue();

        repository.doesLotNumberExist(lotNum, new RepositoryCallback<Boolean>() {
            /**
             * onSuccess
             * @param exists - boolean
             */
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

            /**
             * onError
             * @param message - String
             */
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

    /**
     * createArtifact - creates an artifact with all fields
     * @param lotNum - integer
     */
    private void createArtifact(int lotNum) {
        Artifact newArtifact = new Artifact();
        saveInputs(newArtifact);
        newArtifact.setLotNum(lotNum);

        resolveImage("", String.valueOf(lotNum), imageUrl -> {
            newArtifact.setImage(imageUrl == null ? "" : imageUrl);
            addArtifact(newArtifact);
        });
    }

    /**
     * addArtifact
     * @param newArtifact - Artifact object
     */
    private void addArtifact(Artifact newArtifact)
    {
        repository.addArtifact(
                newArtifact,
                new RepositoryCallback<Void>() {
                    /**
                     * onSuccess
                     * @param result - Void
                     */
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

                    /**
                     * onError - send message
                     * @param message - String
                     */
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

    /**
     * getFormTitle
     * @return - String "Add Artifact
     */
    protected String getFormTitle() {
        return "Add Artifact";
    }
}
