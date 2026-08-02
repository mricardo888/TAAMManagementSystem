package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.List;

public class ExpandedArtifactFragment extends Fragment {
    private RecyclerView commentsRV;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private User currentUser;
    private Artifact currentArtifact;

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

        currentUser = new RegularUser();
        currentArtifact = new Artifact(1, "B", "C", "D", new String[] {"E"}, "F");

        commentsRV = view.findViewById(R.id.comments_section);

        TextView artifactNameText = view.findViewById(R.id.artifact_name);
        TextView descriptionText = view.findViewById(R.id.description_text);
        TextView readMoreText = view.findViewById(R.id.read_more_text);
        TextView categoryText = view.findViewById(R.id.category_text);
        TextView materialsText = view.findViewById(R.id.materials_text);
        TextView dynastyText = view.findViewById(R.id.dynasty_text);

        final boolean[] liked = {false};
        final boolean[] saved = {false};
        boolean[] expanded = {false};
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        setupRecyclerView();

        // Description expanding/collapsing
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

        // Back button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(clickedView -> {
            // TODO: Go back to previous page
        });

        // Like button UNFINISHED
        ImageButton likeButton = view.findViewById(R.id.like_button);
        TextView likeCount = view.findViewById(R.id.likes_counter);
        likeButton.setOnClickListener(clickedView -> {
            liked[0] = !liked[0];

            if (liked[0])
            {
                likeButton.setImageResource(R.drawable.filled_heart_icon);
            }
            else
            {
                likeButton.setImageResource(R.drawable.heart_icon);
            }

            // TODO: Make changes to database
        });

        // Save button UNFINISHED
        ImageButton saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(clickedView -> {
            saved[0] = !saved[0];

            if (saved[0])
            {
                saveButton.setImageResource(R.drawable.filled_bookmark_icon);
            }
            else
            {
                saveButton.setImageResource(R.drawable.bookmark_icon);
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

        // Admin controls

        // Edit button UNFINISHED
        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(clickedView -> {
            EditArtifactDialogFragment dialog =
                    new EditArtifactDialogFragment();

            dialog.setArtifact(currentArtifact, updatedArtifact -> {
                artifactNameText.setText(updatedArtifact.getName());
                descriptionText.setText(updatedArtifact.getDescription());
                categoryText.setText(updatedArtifact.getCategory());
                dynastyText.setText(updatedArtifact.getDynasty());

                String[] materials = updatedArtifact.getMaterials();
                materialsText.setText(materials == null ? "" : String.join(", ", materials));
            });

            dialog.show(getParentFragmentManager(), "EditArtifactDialog");
        });

        editButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

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

        removeButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
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
    }

    private List<Comment> getDummyData() {
        List<Comment> comments = new ArrayList<>();

        RegularUser u1 = new RegularUser("user_001", null);
        RegularUser u2 = new RegularUser("user_001", null);
        RegularUser u3 = new RegularUser("user_001", null);
        RegularUser u4 = new RegularUser("user_001", null);

        comments.add(new Comment(u1, "This artifact looks amazing."));
        comments.add(new Comment(u2, "I really like the glaze and pattern details."));
        comments.add(new Comment(u3, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        comments.add(new Comment(u4, "I would love to see this artifact in person."));

        return comments;
    }
}
