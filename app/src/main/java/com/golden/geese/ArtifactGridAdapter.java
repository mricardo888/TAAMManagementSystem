/*
 * ArtifactGridAdapter
 * Ali Al-Baiti
 * Adapter for the Artifact Grid
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

    /**
     * Primary Constructor
     * @param artifactList - the List of artifacts
     * @param listener - OnGridItemClickListener
     */
    public ArtifactGridAdapter(List<Artifact> artifactList, OnGridItemClickListener listener) {
        this.artifactList = artifactList;
        this.listener = listener;
    }

    /**
     * updateData
     * @param newArtifactList - new List of Artifacts
     */
    public void updateData(List<Artifact> newArtifactList) {
        this.artifactList = newArtifactList;
        notifyDataSetChanged();
    }

    /**
     * onCreateViewHolder
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return - GridViewHolder object
     */
    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artifact_grid, parent, false);
        return new GridViewHolder(view);
    }

    /**
     * onBindViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * getItemCount
     * @return - integer size of the artifactList
     */
    @Override
    public int getItemCount() {
        return artifactList == null ? 0 : artifactList.size();
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGridImage;

        /**
         * GridViewHolder
         * @param itemView - View item
         */
        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGridImage = itemView.findViewById(R.id.iv_grid_image);
        }
    }
}