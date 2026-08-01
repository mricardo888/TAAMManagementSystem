package com.golden.geese;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.golden.geese.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private ViewPager2 viewPagerCarousel;
    private RecyclerView rvArtifacts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Link views to the XML IDs
        viewPagerCarousel = view.findViewById(R.id.viewPager_carousel);
        rvArtifacts = view.findViewById(R.id.rv_artifacts);

        setupCarousel();
        setupRecyclerView();
//        // Access the button directly using type-safe binding property
//        binding.testNavButton1.setOnClickListener(v -> {
//            // Trigger navigation to test nav screen
//            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_testNavFragment);
//        });
    }

    private void setupCarousel() {
        List<Artifact> carouselData = getDummyData();
        ArtifactAdapter carouselAdapter = new ArtifactAdapter(carouselData, R.layout.item_carousel, this::openDetailsScreen);
        viewPagerCarousel.setAdapter(carouselAdapter);

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

    private void setupRecyclerView() {
        List<Artifact> artifactData = getDummyData();
        ArtifactAdapter artifactAdapter = new ArtifactAdapter(artifactData, R.layout.item_artifact, this::openDetailsScreen);

        // Sets up standard horizontal scrolling
        rvArtifacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvArtifacts.setAdapter(artifactAdapter);
    }

    // Generates dummy data to populate the lists
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