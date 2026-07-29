package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExpandedArtifactFragment extends Fragment {
    private RecyclerView commentsRV;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expanded_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstnaceState) {
        super.onViewCreated(view, savedInstnaceState);

        commentsRV = view.findViewById(R.id.comments_section);

        TextView descriptionText = view.findViewById(R.id.description_text);
        TextView readMoreText = view.findViewById(R.id.read_more_text);

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
            // Go back to previous page
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
        });

        // Filter button UNFINISHED
        ImageButton filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(clickedView -> {
            // open filter menu
        });

        // Add comment button UNFINISHED
        ImageButton addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(clickedView -> {
            // open add comment popup
        });

        // Admin controls

        // Edit button UNFINISHED
        ImageButton editButton = view.findViewById(R.id.edit_button);
        backButton.setOnClickListener(clickedView -> {
            // open up edit artifact page
        });
        editButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // Remove button UNFINISHED
        ImageButton removeButton = view.findViewById(R.id.remove_button);
        backButton.setOnClickListener(clickedView -> {
            // open up confirm remove artifact popup
        });
        removeButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView()
    {
        commentsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        List<Comment> comments = getDummyData();
        CommentAdapter commentAdapter = new CommentAdapter(
            comments,
            currentUser,
            (comment, position) -> {}
        );
        commentsRV.setAdapter(commentAdapter);
    }

    private List<Comment> getDummyData() {
        List<Comment> comments = new ArrayList<>();

        comments.add(new Comment("user_001", "This artifact looks amazing."));
        comments.add(new Comment("user_002", "I really like the glaze and pattern details."));
        comments.add(new Comment("user_003", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        comments.add(new Comment("user_004", "I would love to see this artifact in person."));

        return comments;
    }
}
