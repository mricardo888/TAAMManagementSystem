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

import com.golden.geese.databinding.FragmentHomeBinding;
import com.golden.geese.model.ArtifactRepository;
import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

import java.util.List;

public class HomeFragment extends Fragment {
    private User currentUser;
    private ViewPager2 viewPagerCarousel;
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
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        // Link views to the XML IDs
        viewPagerCarousel = view.findViewById(R.id.viewPager_carousel);
        rvArtifacts = view.findViewById(R.id.rv_artifacts);
        Button displayViewAllButton = view.findViewById(R.id.onDisplayViewAllButton);
        Button artifactsViewAllButton = view.findViewById(R.id.artifactsViewAllButton);

        rvArtifacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        setupCarouselTransforms();

        view.findViewById(R.id.tab_profile).setOnClickListener(clickedView -> {
            loadFragment(new ProfileFragment());
        });

        // Add button function
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(clickedView -> showAddArtifactDialog());
        addButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

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
            }
        });

        displayViewAllButton.setOnClickListener(v -> {

        });

        artifactsViewAllButton.setOnClickListener(v -> {
            loadFragment(new BrowseFragment());
        });
    }

    private void setupCarousel(List<Artifact> artifacts) {
        ArtifactAdapter carouselAdapter = new ArtifactAdapter(artifacts, R.layout.item_carousel, this::openDetailsScreen);
        viewPagerCarousel.setAdapter(carouselAdapter);
    }

    private void setupRecyclerView(List<Artifact> artifacts) {
        ArtifactAdapter artifactAdapter = new ArtifactAdapter(artifacts, R.layout.item_artifact, this::openDetailsScreen);
        rvArtifacts.setAdapter(artifactAdapter);
    }

    private void setupCarouselTransforms() {
        // 1. Enable peeking beyond bounds
        viewPagerCarousel.setClipToPadding(false);
        viewPagerCarousel.setClipChildren(false);
        viewPagerCarousel.setOffscreenPageLimit(3);

        // Remove overscroll glow effect
        View child = viewPagerCarousel.getChildAt(0);
        if (child != null) {
            child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // 2. Mathematically calculate dynamic padding once layout is measured
        viewPagerCarousel.post(() -> {
            int containerWidth = viewPagerCarousel.getWidth();
            int containerHeight = viewPagerCarousel.getHeight();

            if (containerWidth == 0 || containerHeight == 0) return;

            // Desired Aspect Ratio (Width / Height). e.g., 3:4 ratio = 0.75f
            float targetAspectRatio = 0.75f;

            // Calculate card width based on available height
            int targetCardWidth = (int) (containerHeight * targetAspectRatio);

            // Calculate side padding required to center a card of exact targetCardWidth
            int sidePadding = (containerWidth - targetCardWidth) / 2;

            if (sidePadding > 0) {
                viewPagerCarousel.setPadding(sidePadding, 0, sidePadding, 0);
            }
        });

        // 3. Set up Transformer for gap spacing and scaling
        CompositePageTransformer transformer = new CompositePageTransformer();

        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            float scale = 0.85f + r * 0.15f;

            // Scale BOTH axes so the card shrinks proportionally
            page.setScaleY(scale);
            page.setScaleX(scale);

            page.setAlpha(0.6f + r * 0.4f);

            int overlapPx = (int) (24 * getResources().getDisplayMetrics().density);
            page.setTranslationX(-position * overlapPx);
            page.setTranslationZ(-Math.abs(position));
        });

        viewPagerCarousel.setPageTransformer(transformer);
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

        dialog.setOnArtifactSavedListener(newArtifact -> {
            loadFragment(new HomeFragment());
        });

        dialog.show(getParentFragmentManager(), "AddArtifactDialog");
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}