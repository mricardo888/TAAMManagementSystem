package com.golden.geese;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ArtifactGridAdapter extends RecyclerView.Adapter<ArtifactGridAdapter.GridViewHolder> {
    private List<Artifact> artifactList;
    private OnGridItemClickListener listener;

    // Interface so clicks on the grid can open the Details screen
    public interface OnGridItemClickListener {
        void onGridItemClick(Artifact artifact);
    }

    public ArtifactGridAdapter(List<Artifact> artifactList, OnGridItemClickListener listener) {
        this.artifactList = artifactList;
        this.listener = listener;
    }

    public void updateData(List<Artifact> newArtifactList) {
        this.artifactList = newArtifactList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artifact_grid, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        Artifact artifact = artifactList.get(position);

        Glide.with(holder.itemView)
                .load(artifact.getImage())
                .placeholder(R.drawable.empty)
                .error(R.drawable.empty)
                .into(holder.ivGridImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGridItemClick(artifact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artifactList == null ? 0 : artifactList.size();
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGridImage;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGridImage = itemView.findViewById(R.id.iv_grid_image);
        }
    }
}