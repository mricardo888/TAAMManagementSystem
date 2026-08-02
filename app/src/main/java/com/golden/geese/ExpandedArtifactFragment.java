package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class ExpandedArtifactFragment extends Fragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();

    private RecyclerView commentsRV;
    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();
    private User currentUser;
    private Artifact artifact;

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

        TextView artifactName = view.findViewById(R.id.artifact_name);
        ImageView artifactImage = view.findViewById(R.id.image_placeholder);
        TextView descriptionText = view.findViewById(R.id.description_text);
        TextView readMoreText = view.findViewById(R.id.read_more_text);
        TextView categoryText = view.findViewById(R.id.category_text);
        TextView materialsText = view.findViewById(R.id.materials_text);
        TextView dynastyText = view.findViewById(R.id.dynasty_text);
        likesCounter = view.findViewById(R.id.likes_counter);
        commentsCounter = view.findViewById(R.id.comments_counter);

        final boolean[] expanded = {false};

        if (artifact != null) {
            artifactName.setText(artifact.getName());
            descriptionText.setText(artifact.getDescription());
            Glide.with(this)
                    .load(artifact.getImage())
                    .placeholder(R.drawable.expanded_artifact_placeholder)
                    .error(R.drawable.expanded_artifact_placeholder)
                    .into(artifactImage);
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
        editButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        ImageButton removeButton = view.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(clickedView -> confirmDeleteArtifact());
        removeButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
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
            }

            // TODO: Make changes to database
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
            }

            // TODO: Make changes to database
        });
        
        // Add comment button UNFINISHED
        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(clickedView -> {
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

                            Comment newComment = new Comment(currentUser, commentText);
                            commentAdapter.addComment(newComment);

                            dialog.dismiss();
                        });
            });

            dialog.show();
        });

    private void toggleLike() {
        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }

        // Edit button UNFINISHED
        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(clickedView -> {
            EditArtifactDialogFragment dialog =
                    new EditArtifactDialogFragment();

            dialog.setArtifact(artifact, updatedArtifact -> {
                artifactName.setText(updatedArtifact.getName());
                descriptionText.setText(updatedArtifact.getDescription());
                categoryText.setText(updatedArtifact.getCategory());
                dynastyText.setText(updatedArtifact.getDynasty());

                String[] materials = updatedArtifact.getMaterials();
                materialsText.setText(materials == null ? "" : String.join(", ", materials));
            });

            dialog.show(getParentFragmentManager(), "EditArtifactDialog");
        });

        // Remove button UNFINISHED
        ImageButton removeButton = view.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(clickedView -> {
            new MaterialAlertDialogBuilder(requireContext(), R.style.CustomDeleteDialog)
                    .setTitle("Delete artifact?")
                    .setMessage("This action cannot be undone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // TODO: Delete artifact from database
                    })
                    .show();
        });
    }

    private void setupRecyclerView()
    {
        commentsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        comments = getDummyData();
        commentAdapter = new CommentAdapter(
            comments,
            currentUser,
            (comment, position) -> {}
        );
        commentsRV.setAdapter(commentAdapter);
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
            }
        };

        if (liked) {
            repository.unlikeArtifact(artifact.getLotNum(), uid, callback);
        } else {
            repository.likeArtifact(artifact.getLotNum(), uid, callback);
        }
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
            }
        });
    }

    private void showAddCommentDialog() {
        if (currentUser == null || artifact == null) {
            return;
        }

        EditText input = new EditText(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Comment")
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        addComment(text);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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
            }
        });
    }

    private void onDeleteComment(Comment comment, int position) {
        repository.deleteComment(artifact.getLotNum(), comment.getCommentId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    commentsCounter.setText(String.valueOf(comments.size()));
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void confirmDeleteArtifact() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Artifact")
                .setMessage("Are you sure you want to delete this artifact? This cannot be undone.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteArtifact())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteArtifact() {
        repository.deleteArtifact(artifact.getLotNum(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    getParentFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }
}
