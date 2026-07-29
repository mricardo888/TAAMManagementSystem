package com.golden.geese;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private User currentUser;
    private OnCommentDeleteListener deleteListener;

    public interface OnCommentDeleteListener
    {
        void onDeleteComment(Comment comment, int position);
    }

    public CommentAdapter(List<Comment> comments, User currentUser, OnCommentDeleteListener deleteListener)
    {
        this.comments = comments;
        this.currentUser = currentUser;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.username.setText(comment.getAuthorId());
        holder.body.setText(comment.getText());
        holder.profileImage.setImageResource(R.drawable.placeholder_pfp);

        if (comment.getTimestamp() != null)
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
            holder.time.setText(comment.getTimestamp().format(formatter));
        }
        else
        {
            holder.time.setText("");
        }

        boolean isOwner = currentUser != null && currentUser.getUsername() != null && currentUser.getUsername().equals(comment.getAuthorId());
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        holder.deleteButton.setVisibility((isOwner || isAdmin) ? View.VISIBLE : View.GONE);

        holder.deleteButton.setOnClickListener(clickedView -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Comment commentToDelete = comments.get(adapterPosition);

            if (deleteListener != null)
            {
                deleteListener.onDeleteComment(commentToDelete, adapterPosition);
            }

            removeComment(adapterPosition);
        });

    }

    @Override
    public int getItemCount() {
        return comments == null ? 0 : comments.size();
    }

    public void removeComment(int position)
    {
        if (comments == null)
        {
            return;
        }

        if (position < 0 || position >= comments.size())
        {
            return;
        }

        comments.remove(position);
        notifyItemRemoved(position);
    }

    public void addComment(Comment comment) {
        if (comment == null)
        {
            return;
        }

        comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView username;
        private TextView body;
        private TextView time;
        private ImageButton deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.comment_profile_picture);
            username = itemView.findViewById(R.id.comment_username);
            body = itemView.findViewById(R.id.comment_body);
            time = itemView.findViewById(R.id.comment_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
