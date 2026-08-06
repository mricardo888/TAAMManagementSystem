package com.golden.geese;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.golden.geese.databinding.FragmentHomeBinding;
import com.golden.geese.model.ArtifactRepository;
import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the home screen.
 */
public class HomeFragment extends Fragment {
    private User currentUser;
    private TextView welcomeText;
    private ViewPager2 viewPagerCarousel;
    private RecyclerView rvArtifacts;
    private ImageButton addButton;
    private final ArtifactRepository artifactRepository = new FirebaseArtifactRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Sets up the interactable elements of the screen.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.home_fragment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUser = SessionManager.getInstance().getCurrentUser();
        boolean canManage = currentUser != null && currentUser.canManageArtifacts();

        // Link views to the XML IDs
        welcomeText = view.findViewById(R.id.tv_welcome);
        welcomeText.setText(getString(R.string.welcome_user) + (currentUser == null ? "" : currentUser.getUsername()));

        viewPagerCarousel = view.findViewById(R.id.viewPager_carousel);

        rvArtifacts = view.findViewById(R.id.rv_artifacts);
        rvArtifacts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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
        addButton.setVisibility(canManage ? View.VISIBLE : View.GONE);

        displayViewAllButton.setOnClickListener(v -> {
            loadFragment(BrowseFragment.newInstance(BrowseFragment.FILTER_ON_DISPLAY));
        });

        artifactsViewAllButton.setOnClickListener(v -> {
            loadFragment(new BrowseFragment());
        });
    }

    /**
     * Reloads the artifacts when the view is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        loadArtifacts();
    }

    /**
     * Loads all artifacts from the database and sets up the corresponding views.
     */
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

    /**
     * Sets up the on display carousel.
     * @param artifacts list of artifacts to be displayed.
     */
    private void setupCarousel(List<Artifact> artifacts) {
        List<Artifact> onDisplayArtifacts = new ArrayList<>();
        for (Artifact artifact : artifacts){
            if(artifact.isOnDisplay()){
                onDisplayArtifacts.add(artifact);
            }
        }

        ArtifactAdapter carouselAdapter = new ArtifactAdapter(onDisplayArtifacts, R.layout.item_carousel, this::openDetailsScreen);
        viewPagerCarousel.setAdapter(carouselAdapter);

        viewPagerCarousel.post(() -> {
            if (!viewPagerCarousel.isFakeDragging()) {
                viewPagerCarousel.beginFakeDrag();
                viewPagerCarousel.fakeDragBy(0f); // 0 pixel drag
                viewPagerCarousel.endFakeDrag();
            }
        });
    }

    /**
     * Sets up the all artifacts recycler view.
     * @param artifacts list of artifacts to be displayed.
     */
    private void setupRecyclerView(List<Artifact> artifacts) {
        ArtifactAdapter artifactAdapter = new ArtifactAdapter(artifacts, R.layout.item_artifact, this::openDetailsScreen);
        rvArtifacts.setAdapter(artifactAdapter);
    }

    /**
     * Sets up the automatic scaling of the cards based on their position.
     */
    private void setupCarouselTransforms() {
        // This makes the entire screen a valid touch zone.
        viewPagerCarousel.setClipToPadding(false);
        viewPagerCarousel.setClipChildren(false);
        viewPagerCarousel.setPadding(0, 0, 0, 0);
        viewPagerCarousel.setOffscreenPageLimit(3);

        View child = viewPagerCarousel.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOverScrollMode(View.OVER_SCROLL_NEVER);
            child.setPadding(0, 0, 0, 0);

        }

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {

            float centerScale = 0.75f; // Center card takes up 75% of the screen width
            float sideScale = 0.60f;   // Side cards take up 60% of the screen width

            float r = 1 - Math.abs(position);
            r = Math.max(0, Math.min(1, r)); // Clamp between 0 and 1

            float scale = sideScale + r * (centerScale - sideScale);
            page.setScaleY(scale);
            page.setScaleX(scale);
            page.setAlpha(0.35f + r * 0.65f);

            float pageW = page.getWidth();
            float emptySpaceGap = (pageW * (1 - centerScale) / 2f) + (pageW * (1 - sideScale) / 2f);

            // add desired 80dp overlap effect
            int overlapPx = (int) (80 * page.getResources().getDisplayMetrics().density);

            float shiftOffset = emptySpaceGap + overlapPx;
            page.setTranslationX(-position * shiftOffset);

            float zIndex = 100f - (Math.abs(position) * 100f);
            page.setTranslationZ(zIndex);

            // Explicitly override the native MaterialCardView elevation shadow
            if (page instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) page).setCardElevation(Math.max(1f, zIndex));
            }
        });

        viewPagerCarousel.setPageTransformer(transformer);
    }

    /**
     * Navigates to the details screen of an artifact.
     * @param artifact artifact to be passed to the details screen.
     */
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

    /**
     * Displays the dialogue to add a new artifact to the admin user.
     */
    private void showAddArtifactDialog() {
        AddArtifactDialogFragment dialog = new AddArtifactDialogFragment();

        dialog.setOnArtifactSavedListener(newArtifact -> loadArtifacts());

        dialog.show(getParentFragmentManager(), "AddArtifactDialog");
    }

    /**
     * Navigates to a new fragment screen.
     * @param fragment the target fragment screen.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Displays an error message to the user.
     * @param what The error itself.
     * @param reason The reason the error happened.
     */
    private void showError(String what, String reason) {
        if (!isAdded()) {
            return;
        }
        String detail = (reason == null || reason.trim().isEmpty()) ? "" : ": " + reason;
        Toast.makeText(requireContext(), what + detail, Toast.LENGTH_SHORT).show();
    }
}