package com.golden.geese; // Replace with your actual package name

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivDetailImage = view.findViewById(R.id.iv_image);
        TextView tvDetailTitle = view.findViewById(R.id.tv_name);

        if (getArguments() != null) {
            Artifact artifact = (Artifact) getArguments().getSerializable("Artifact");

            tvDetailTitle.setText(artifact.getName());
            ivDetailImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }
}