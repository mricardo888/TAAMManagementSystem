/*
 * ArtifactAdapter
 * Ali Al-Baiti
 * Adapter class for Artifacts
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

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {
    public interface OnArtifactClickListener {
        void onClick(Artifact artifact);
    }

    private List<Artifact> artifactList;
    private int layoutId;
    private OnArtifactClickListener clickListener;

    /**
     * Primary Constructor
     * @param artifactList - type List<Artifact>
     * @param layoutId - integer ID
     */
    public ArtifactAdapter(List<Artifact> artifactList, int layoutId) {
        this(artifactList, layoutId, null);
    }

    /**
     * Secondary Constructor
     * @param artifactList - type List<Artifact>
     * @param layoutId - integer ID
     * @param clickListener - OnArtifactClickListener
     */
    public ArtifactAdapter(List<Artifact> artifactList, int layoutId, OnArtifactClickListener clickListener) {
        this.artifactList = artifactList;
        this.layoutId = layoutId;
        this.clickListener = clickListener;
    }

    /**
     * onCreateViewHolder
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return - object type ArtifactViewHolder
     */
    @NonNull
    @Override
    public ArtifactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new ArtifactViewHolder(view);
    }

    /**
     * onBindViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ArtifactViewHolder holder, int position) {
        Artifact artifact = artifactList.get(position);

        holder.tvName.setText(artifact.getName());

        Glide.with(holder.itemView)
                .load(artifact.getImage())
                .placeholder(R.drawable.empty)
                .error(R.drawable.empty)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(artifact);
            }
        });
    }

    /**
     * getItemCount
     * @return - integer size of artifactList
     */
    @Override
    public int getItemCount() {
        return artifactList == null ? 0 : artifactList.size();
    }

    static class ArtifactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;

        /**
         * Primary Constructor
         * @param itemView - type View
         */
        public ArtifactViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match exactly what is in your item_artifact.xml
            ivImage = itemView.findViewById(R.id.iv_artifact_image);
            tvName = itemView.findViewById(R.id.tv_artifact_title);
        }
    }
}
