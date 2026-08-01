package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private RecyclerView likedArtifactsRV;
    private RecyclerView savedArtifactsRV;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstnaceState) {
        super.onViewCreated(view, savedInstnaceState);

        likedArtifactsRV = view.findViewById(R.id.liked_artifact_scroller);

        savedArtifactsRV = view.findViewById(R.id.saved_artifact_scroller);

        setupRecyclerViews();
    }

    private void setupRecyclerViews()
    {
        likedArtifactsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        savedArtifactsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Artifact> likedArtifacts = getDummyData();
        List<Artifact> savedArtifacts = getDummyData();

        ArtifactAdapter likedAdapter = new ArtifactAdapter(likedArtifacts, R.layout.item_artifact, this::openDetailsScreen);
        ArtifactAdapter savedAdapter = new ArtifactAdapter(savedArtifacts, R.layout.item_artifact, this::openDetailsScreen);

        likedArtifactsRV.setAdapter(likedAdapter);
        savedArtifactsRV.setAdapter(savedAdapter);
    }

    private List<Artifact> getDummyData() {
        List<Artifact> list = new ArrayList<>();
        // Note: Change R.drawable.sample_image to the actual name of your image file in res/drawable
        list.add(new Artifact(0, "A Tang 'Sancai'\n'Baoxianghua' Box", "", "", null, "", "", null, "", "", "", "", 0, "", null));
        list.add(new Artifact(0, "A Tang 'Sancai'\n'Baoxianghua' Box", "", "", null, "", "", null, "", "", "", "", 0, "", null));
        list.add(new Artifact(0, "A Tang 'Sancai'\n'Baoxianghua' Box", "", "", null, "", "", null, "", "", "", "", 0, "", null));
        list.add(new Artifact(0, "A Tang 'Sancai'\n'Baoxianghua' Box", "", "", null, "", "", null, "", "", "", "", 0, "", null));
        return list;
    }

    private void openDetailsScreen(Artifact artifact) {
        DetailsFragment detailsFragment = DetailsFragment.newInstance(artifact);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
