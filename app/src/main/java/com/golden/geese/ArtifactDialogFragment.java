package com.golden.geese;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.golden.geese.storage.ArtifactImageUploader;
import com.golden.geese.storage.ImageUploadCallback;

public abstract class ArtifactDialogFragment extends DialogFragment {
    protected TextView formTitle;
    protected EditText lotNumberInput;
    protected EditText nameInput;
    protected EditText descriptionInput;
    protected Spinner categoryInput;
    protected Spinner materialInput;
    protected Spinner dynastyInput;
    protected ImageView imagePreview;
    protected Uri selectedImageUri;

    private ArtifactImageUploader imageUploader;

    protected OnArtifactSavedListener savedListener;

    public interface OnArtifactSavedListener {
        void onArtifactSaved(Artifact artifact);
    }

    protected interface ImageResolvedCallback {
        void onResolved(String imageUrl);
    }

    public void setOnArtifactSavedListener(OnArtifactSavedListener listener) {
        this.savedListener = listener;
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(imagePreview);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_artifact_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        formTitle = view.findViewById(R.id.form_title);
        lotNumberInput = view.findViewById(R.id.artifact_lot_number);
        nameInput = view.findViewById(R.id.artifact_name);
        descriptionInput = view.findViewById(R.id.artifact_description);
        categoryInput = view.findViewById(R.id.artifact_category);
        materialInput = view.findViewById(R.id.artifact_material);
        dynastyInput = view.findViewById(R.id.artifact_dynasty);
        imagePreview = view.findViewById(R.id.edit_image_preview);
        Button changeImageButton = view.findViewById(R.id.change_image_button);

        formTitle.setText(getFormTitle());

        setupSpinner(categoryInput, R.array.category_options);
        setupSpinner(materialInput, R.array.material_options);
        setupSpinner(dynastyInput, R.array.dynasty_options);

        if (!isLotNumberEditable()) {
            lotNumberInput.setEnabled(false);
            lotNumberInput.setAlpha(0.6f);
        }

        imageUploader = new ArtifactImageUploader(requireContext());
        changeImageButton.setOnClickListener(clickedView -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Lot number is the Firebase key for an artifact, so it can only be set once, at creation.
     */
    protected boolean isLotNumberEditable() {
        return true;
    }

    protected boolean validateFields() {
        String lotNumberText = lotNumberInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        boolean valid = true;

        if (lotNumberText.isEmpty()) {
            lotNumberInput.setError("Lot number is required");
            valid = false;
        } else {
            try {
                if (Integer.parseInt(lotNumberText) <= 0) {
                    lotNumberInput.setError("Lot number must be a positive number");
                    valid = false;
                } else {
                    lotNumberInput.setError(null);
                }
            } catch (NumberFormatException e) {
                lotNumberInput.setError("Lot number must be a valid number");
                valid = false;
            }
        }

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

        boolean categorySelected = categoryInput.getSelectedItemPosition() > 0;
        boolean materialSelected = materialInput.getSelectedItemPosition() > 0;
        boolean dynastySelected = dynastyInput.getSelectedItemPosition() > 0;

        if (!categorySelected || !materialSelected || !dynastySelected) {
            valid = false;
            Toast.makeText(
                    requireContext(),
                    "Please select a category, material, and dynasty/period",
                    Toast.LENGTH_SHORT
            ).show();
        }

        if (!valid) {
            focusInvalidField();
        }

        return valid;
    }

    private void focusInvalidField() {
        if (lotNumberInput.getError() != null) {
            lotNumberInput.requestFocus();
        } else if (nameInput.getError() != null) {
            nameInput.requestFocus();
        } else if (descriptionInput.getError() != null) {
            descriptionInput.requestFocus();
        }
    }

    protected void fillFields(@NonNull Artifact artifact)
    {
        lotNumberInput.setText(String.valueOf(artifact.getLotNum()));
        nameInput.setText(artifact.getName());
        descriptionInput.setText(artifact.getDescription());
        setSpinnerSelection(categoryInput, artifact.getCategory());
        setSpinnerSelection(materialInput, artifact.getMaterial());
        setSpinnerSelection(dynastyInput, artifact.getDynasty());

        Glide.with(this)
                .load(artifact.getImage())
                .placeholder(R.drawable.expanded_artifact_placeholder)
                .error(R.drawable.expanded_artifact_placeholder)
                .into(imagePreview);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        spinner.setSelection(Math.max(position, 0));
    }

    protected void saveInputs(@NonNull Artifact artifact) {
        artifact.setName(nameInput.getText().toString().trim());
        artifact.setDescription(descriptionInput.getText().toString().trim());
        artifact.setCategory(categoryInput.getSelectedItem().toString());
        artifact.setMaterial(materialInput.getSelectedItem().toString());
        artifact.setDynasty(dynastyInput.getSelectedItem().toString());
    }

    protected int getLotNumberValue() {
        return Integer.parseInt(lotNumberInput.getText().toString().trim());
    }

    /**
     * Uploads the newly picked image (if any) and hands back the URL to use for the artifact;
     * falls back to existingImageUrl untouched when no new image was picked.
     */
    protected void resolveImage(String existingImageUrl, String lotNumberForPath, ImageResolvedCallback callback) {
        if (selectedImageUri == null) {
            callback.onResolved(existingImageUrl);
            return;
        }

        imageUploader.uploadArtifactImage(selectedImageUri, lotNumberForPath, new ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                if (!isAdded()) {
                    return;
                }
                callback.onResolved(imageUrl);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(
                        requireContext(),
                        errorMessage == null ? "Failed to upload image" : errorMessage,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
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

    protected abstract String getFormTitle();
}
