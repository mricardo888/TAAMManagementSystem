package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.bumptech.glide.Glide;
import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;
import com.golden.geese.storage.ArtifactImageUploader;
import com.golden.geese.storage.ImageDeleteCallback;

import java.util.ArrayList;
import java.util.List;

public class ExpandedArtifactFragment extends Fragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();

    private RecyclerView commentsRV;
    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();
    private User currentUser;
    private Artifact artifact;
    private TextView artifactName;
    private ImageView artifactImage;
    private TextView descriptionText;
    private TextView lotNumberText;
    private TextView categoryText;
    private TextView materialText;
    private TextView dynastyText;
    private TextView originText;
    private TextView dimensionsText;
    private TextView conditionReportText;
    private TextView locationText;
    private TextView acqMethodText;
    private TextView provenanceText;
    private TextView accessionNumberText;
    private TextView notesText;
    private ImageButton likeButton;
    private TextView likesCounter;
    private ImageButton saveButton;
    private TextView commentsCounter;
    private boolean liked = false;
    private boolean saved = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expanded_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.expanded_artifact_fragment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        if (getArguments() != null) {
            artifact = (Artifact) getArguments().getSerializable("Artifact");
        }

        commentsRV = view.findViewById(R.id.comments_section);
        commentsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        commentAdapter = new CommentAdapter(comments, currentUser, this::onDeleteComment);
        commentsRV.setAdapter(commentAdapter);

        artifactName = view.findViewById(R.id.artifact_name);
        artifactImage = view.findViewById(R.id.image_placeholder);
        descriptionText = view.findViewById(R.id.description_text);
        TextView readMoreText = view.findViewById(R.id.read_more_text);
        lotNumberText = view.findViewById(R.id.lot_number_text);
        categoryText = view.findViewById(R.id.category_text);
        materialText = view.findViewById(R.id.material_text);
        dynastyText = view.findViewById(R.id.dynasty_text);
        originText = view.findViewById(R.id.origin_text);
        dimensionsText = view.findViewById(R.id.dimensions_text);
        conditionReportText = view.findViewById(R.id.condition_report_text);
        locationText = view.findViewById(R.id.location_text);
        acqMethodText = view.findViewById(R.id.acq_method_text);
        provenanceText = view.findViewById(R.id.provenance_text);
        accessionNumberText = view.findViewById(R.id.accession_number_text);
        notesText = view.findViewById(R.id.notes_text);
        likesCounter = view.findViewById(R.id.likes_counter);
        commentsCounter = view.findViewById(R.id.comments_counter);

        final boolean[] expanded = {false};

        if (artifact != null) {
            bindArtifact(artifact);
            loadLikeState();
            loadSaveState();
            loadComments();
        }

        readMoreText.setOnClickListener(clickedView -> {
            expanded[0] = !expanded[0];

            if (expanded[0])
            {
                descriptionText.setMaxLines(Integer.MAX_VALUE);
                readMoreText.setText(R.string.read_less);
            }
            else
            {
                descriptionText.setMaxLines(6);
                readMoreText.setText(R.string.read_more);
            }
        });

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickedView -> getParentFragmentManager().popBackStack());

        likeButton = view.findViewById(R.id.like_button);
        likeButton.setOnClickListener(clickedView -> toggleLike());

        saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(clickedView -> toggleSave());

        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(clickedView -> showAddCommentDialog());

        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(clickedView -> showEditArtifactDialog());
        editButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        ImageButton removeButton = view.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(clickedView -> confirmDeleteArtifact());
        removeButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void bindArtifact(@NonNull Artifact toShow) {
        artifactName.setText(toShow.getName());
        descriptionText.setText(toShow.getDescription());
        lotNumberText.setText(String.valueOf(toShow.getLotNum()));
        categoryText.setText(valueOrDash(toShow.getCategory()));
        materialText.setText(valueOrDash(toShow.getMaterial()));
        dynastyText.setText(valueOrDash(toShow.getDynasty()));
        originText.setText(valueOrDash(toShow.getOrigin()));
        dimensionsText.setText(formatDimensions(toShow.getDimensions()));
        conditionReportText.setText(valueOrDash(toShow.getConditionReport()));
        locationText.setText(valueOrDash(toShow.getLocation()));
        acqMethodText.setText(valueOrDash(toShow.getAcqMethod()));
        provenanceText.setText(valueOrDash(toShow.getProvenance()));
        accessionNumberText.setText(
                toShow.getAccessionNum() == 0
                        ? getString(R.string.ea_not_recorded)
                        : String.valueOf(toShow.getAccessionNum()));
        notesText.setText(valueOrDash(toShow.getNotes()));

        Glide.with(this)
                .load(toShow.getImage())
                .placeholder(R.drawable.expanded_artifact_placeholder)
                .error(R.drawable.expanded_artifact_placeholder)
                .into(artifactImage);
    }

    private String valueOrDash(String value) {
        return (value == null || value.trim().isEmpty()) ? getString(R.string.ea_not_recorded) : value;
    }

    private String formatDimensions(List<Double> dimensions) {
        if (dimensions == null || dimensions.size() < 3) {
            return getString(R.string.ea_not_recorded);
        }

        boolean allZero = true;
        for (Double dimension : dimensions) {
            if (dimension != null && dimension != 0.0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            return getString(R.string.ea_not_recorded);
        }

        return getString(
                R.string.ea_dimensions_format,
                trimDecimal(dimensions.get(0)),
                trimDecimal(dimensions.get(1)),
                trimDecimal(dimensions.get(2)));
    }

    private String trimDecimal(Double value) {
        if (value == null) {
            return "0";
        }
        return value == Math.floor(value) ? String.valueOf(value.longValue()) : String.valueOf(value);
    }

    private void loadLikeState() {
        repository.getLikeCount(artifact.getLotNum(), new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (isAdded()) {
                    likesCounter.setText(String.valueOf(count));
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not load likes", message);
            }
        });

        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }

        repository.isArtifactLikedByUser(artifact.getLotNum(), currentUser.getUid(), new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isLiked) {
                if (isAdded()) {
                    liked = isLiked;
                    likeButton.setImageResource(liked ? R.drawable.filled_heart_icon : R.drawable.heart_icon);
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not load likes", message);
            }
        });
    }

    private void showAddCommentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_comment, null);

        EditText commentInput = dialogView.findViewById(R.id.comment_input);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.CustomDeleteDialog
                )
                        .setTitle(R.string.add_comment)
                        .setView(dialogView)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.post, null)
                        .create();

        dialog.setOnShowListener(unused -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(buttonView -> {
                        String commentText = commentInput.getText().toString().trim();

                        if (commentText.isEmpty()) {
                            commentInput.setError("Comment cannot be empty");
                            return;
                        }
                        addComment(commentText);

                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void toggleLike() {
        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }
        String uid = currentUser.getUid();
        RepositoryCallback<Void> callback = new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    loadLikeState();
                }
            }

            @Override
            public void onError(String message) {
                showError(liked ? "Could not unlike this artifact" : "Could not like this artifact", message);
            }
        };

        if (liked) {
            repository.unlikeArtifact(artifact.getLotNum(), uid, callback);
        } else {
            repository.likeArtifact(artifact.getLotNum(), uid, callback);
        }
    }

    private void showEditArtifactDialog() {
        EditArtifactDialogFragment dialog = new EditArtifactDialogFragment();

        dialog.setArtifact(artifact);
        dialog.setOnArtifactSavedListener(updatedArtifact -> {
            artifact = updatedArtifact;
            bindArtifact(updatedArtifact);
        });

        dialog.show(getParentFragmentManager(), "EditArtifactDialog");
    }

    private void confirmDeleteArtifact() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDeleteDialog)
                .setTitle("Delete artifact?")
                .setMessage("Are you sure you want to delete this artifact? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteArtifact())
                .show();
    }

    private void loadSaveState() {
        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }

        repository.isArtifactSavedByUser(artifact.getLotNum(), currentUser.getUid(), new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isSaved) {
                if (isAdded()) {
                    saved = isSaved;
                    saveButton.setImageResource(saved ? R.drawable.filled_bookmark_icon : R.drawable.bookmark_icon);
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not load your saved artifacts", message);
            }
        });
    }

    private void toggleSave() {
        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }

        String uid = currentUser.getUid();
        RepositoryCallback<Void> callback = new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    loadSaveState();
                }
            }

            @Override
            public void onError(String message) {
                showError(saved ? "Could not unsave this artifact" : "Could not save this artifact", message);
            }
        };

        if (saved) {
            repository.unsaveArtifact(artifact.getLotNum(), uid, callback);
        } else {
            repository.saveArtifact(artifact.getLotNum(), uid, callback);
        }
    }

    private void loadComments() {
        repository.getComments(artifact.getLotNum(), new RepositoryCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                if (!isAdded()) {
                    return;
                }
                comments = result;
                commentAdapter = new CommentAdapter(comments, currentUser, ExpandedArtifactFragment.this::onDeleteComment);
                commentsRV.setAdapter(commentAdapter);
                commentsCounter.setText(String.valueOf(comments.size()));
            }

            @Override
            public void onError(String message) {
                showError("Could not load comments", message);
            }
        });
    }

    private void addComment(String text) {
        Comment comment = new Comment(currentUser, text);
        repository.addComment(artifact.getLotNum(), comment, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    loadComments();
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not post your comment", message);
            }
        });
    }

    private void onDeleteComment(Comment comment, int position) {
        repository.deleteComment(artifact.getLotNum(), comment.getCommentId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    loadComments();
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not delete that comment", message);
            }
        });
    }

    private void deleteArtifact() {
        repository.deleteArtifact(artifact.getLotNum(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    String imageUrl = artifact.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        new ArtifactImageUploader(requireContext())
                                .deleteArtifactImage(imageUrl, new ImageDeleteCallback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                    }
                                });
                    }
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String message) {
                showError("Could not delete this artifact", message);
            }
        });
    }

    private void showError(String what, String reason) {
        if (!isAdded()) {
            return;
        }
        String detail = (reason == null || reason.trim().isEmpty()) ? "" : ": " + reason;
        Toast.makeText(requireContext(), what + detail, Toast.LENGTH_SHORT).show();
    }
}
