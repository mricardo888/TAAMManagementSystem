package com.golden.geese;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {
    private List<Artifact> artifactList;
    private int layoutId;
    private onArtifactClickListener listener;

    public interface onArtifactClickListener {
        void onArtifactClick(Artifact artifact);
    }

    public ArtifactAdapter(List<Artifact> artifactList, int layoutId, onArtifactClickListener listener) {
        this.artifactList = artifactList;
        this.layoutId = layoutId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtifactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new ArtifactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtifactViewHolder holder, int position) {
        Artifact artifact = artifactList.get(position);

        holder.tvName.setText(artifact.getName());

        // Sets a local drawable resource.
        // If loading from the internet later, use Glide: Glide.with(holder.itemView).load(artifact.getImageUrl()).into(holder.ivImage);
        holder.ivImage.setImageResource(R.drawable.ic_launcher_background);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null) {
                listener.onArtifactClick(artifact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artifactList == null ? 0 : artifactList.size();
    }

    static class ArtifactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        public ArtifactViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match exactly what is in your item_artifact.xml
            ivImage = itemView.findViewById(R.id.iv_artifact_image);
            tvName = itemView.findViewById(R.id.tv_artifact_title);
        }
    }
}
