package com.golden.geese;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast;

import com.golden.geese.databinding.FragmentHomeBinding;
import com.golden.geese.model.ArtifactRepository;
import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private User currentUser;
    private RecyclerView viewPagerCarousel; // changed to RecyclerView from previous ViewPager2
    private RecyclerView rvArtifacts;
    private ImageButton addButton;
    private final ArtifactRepository artifactRepository = new FirebaseArtifactRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SessionManager.getInstance().getCurrentUser();
        boolean canManage = currentUser != null && currentUser.canManageArtifacts();

        // Link views to the XML IDs
        viewPagerCarousel = view.findViewById(R.id.viewPager_carousel);
        viewPagerCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        rvArtifacts = view.findViewById(R.id.rv_artifacts);
        Button displayViewAllButton = view.findViewById(R.id.onDisplayViewAllButton);
        Button artifactsViewAllButton = view.findViewById(R.id.artifactsViewAllButton);

        rvArtifacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        view.findViewById(R.id.tab_profile).setOnClickListener(clickedView -> {
            loadFragment(new ProfileFragment());
        });

        // Add button function
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(clickedView -> showAddArtifactDialog());
        addButton.setVisibility(canManage ? View.VISIBLE : View.GONE);

        displayViewAllButton.setOnClickListener(v -> loadFragment(new BrowseFragment()));

        artifactsViewAllButton.setOnClickListener(v -> {
            loadFragment(new BrowseFragment());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadArtifacts();
    }

    private void loadArtifacts() {
        artifactRepository.getAllArtifacts(new RepositoryCallback<List<Artifact>>() {
            @Override
            public void onSuccess(List<Artifact> artifacts) {
                if (!isAdded()) {
                    return;
                }
                setupCarousel(artifacts);
                setupRecyclerView(artifacts);
            }

            @Override
            public void onError(String message) {
                showError("Could not load artifacts", message);
            }
        });
    }

    //Hard coded list of artifacts "on display"
    private static final int[] ON_DISPLAY_INDICES = { 2, 0, 4, 15, 9, 7, 1, 8, 12 };

    private void setupCarousel(List<Artifact> artifacts) {
        List<Artifact> onDisplayArtifacts = new ArrayList<>();
        for (int index : ON_DISPLAY_INDICES) {
            if (index >= 0 && index < artifacts.size()) {
                onDisplayArtifacts.add(artifacts.get(index));
            }
        }

        ArtifactAdapter carouselAdapter = new ArtifactAdapter(onDisplayArtifacts, R.layout.item_carousel, this::openDetailsScreen);
        viewPagerCarousel.setAdapter(carouselAdapter);
    }
    private void setupRecyclerView(List<Artifact> artifacts) {
        ArtifactAdapter artifactAdapter = new ArtifactAdapter(artifacts, R.layout.item_artifact, this::openDetailsScreen);
        rvArtifacts.setAdapter(artifactAdapter);
    }

    private void openDetailsScreen(Artifact artifact) {
        Bundle args = new Bundle();
        args.putSerializable("Artifact", artifact);

        ExpandedArtifactFragment fragment = new ExpandedArtifactFragment();
        fragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showAddArtifactDialog() {
        AddArtifactDialogFragment dialog = new AddArtifactDialogFragment();

        dialog.setOnArtifactSavedListener(newArtifact -> loadArtifacts());

        dialog.show(getParentFragmentManager(), "AddArtifactDialog");
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showError(String what, String reason) {
        if (!isAdded()) {
            return;
        }
        String detail = (reason == null || reason.trim().isEmpty()) ? "" : ": " + reason;
        Toast.makeText(requireContext(), what + detail, Toast.LENGTH_SHORT).show();
    }
}