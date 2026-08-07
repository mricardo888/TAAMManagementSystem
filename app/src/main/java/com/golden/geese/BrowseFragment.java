package com.golden.geese;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.golden.geese.model.ArtifactRepository;
import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the Browse screen.
 * <p>Allows for substring searching and sorting artifacts</p>
 */
public class BrowseFragment extends Fragment {
    private static final int PAGE_SIZE_ALL = -1;

    private static final String SORT_NAME_ASC = "Name (A-Z)";
    private static final String SORT_NAME_DESC = "Name (Z-A)";

    private static final String ARG_FILTER_MODE = "filterMode";
    public static final String FILTER_LIKED = "LIKED";
    public static final String FILTER_SAVED = "SAVED";
    public static final String FILTER_ON_DISPLAY = "ON_DISPLAY";

    private final ArtifactRepository artifactRepository = new FirebaseArtifactRepository();
    private String filterMode;
    private RecyclerView rvArtifactGrid;
    private TextView pageTracker;
    private int paginationStartIndex = 0;
    private int numArtifacts = 0;
    private int currentPageSize = 12;
    private int pageNumber = 1;
    private String sortMethod = SORT_NAME_ASC;
    private String currentSearch = "";

    /**
     * Initializes a new instance of the class with a filterMode to specify which artfacts are seen.
     * Possible modes:
     * "LIKED"
     * "SAVED"
     * "ON_DISPLAY"
     * @param filterMode
     * @return An instance of BrowseFragment with a specified mode.
     */
    public static BrowseFragment newInstance(String filterMode) {
        BrowseFragment fragment = new BrowseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_MODE, filterMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
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

        filterMode = getArguments() != null ? getArguments().getString(ARG_FILTER_MODE) : null;

        rvArtifactGrid = view.findViewById(R.id.rv_artifact_grid);

        ImageButton backButton = view.findViewById(R.id.back_nav);
        EditText search = view.findViewById(R.id.et_search);
        MaterialButton sortButton = view.findViewById(R.id.btn_sort);
        MaterialButton pageSizeButton = view.findViewById(R.id.btn_page_size);
        MaterialButton backPageButton = view.findViewById(R.id.btn_page_back);
        MaterialButton nextPageButton = view.findViewById(R.id.btn_page_next);
        pageTracker = view.findViewById(R.id.tv_page_number);

        rvArtifactGrid.setLayoutManager(new GridLayoutManager(requireContext(), 3));


        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentSearch = charSequence.toString().toLowerCase();
                resetGrid();
            }
        });

        sortButton.setOnClickListener(v -> {
            PopupMenu sortPopUp = new PopupMenu(requireContext(), v);
            sortPopUp.getMenu().add(SORT_NAME_ASC);
            sortPopUp.getMenu().add(SORT_NAME_DESC);

            sortPopUp.setOnMenuItemClickListener(item -> {
                sortMethod = item.getTitle().toString();
                resetGrid();
                return true;
            });
            sortPopUp.show();
        });

        pageSizeButton.setOnClickListener(v -> {
            PopupMenu pageSizePopup = new PopupMenu(requireContext(), v);
            pageSizePopup.getMenu().add("12 per page");
            pageSizePopup.getMenu().add("24 per page");
            pageSizePopup.getMenu().add("All");

            pageSizePopup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                int chosenPageSize = title.equals("All")
                        ? PAGE_SIZE_ALL
                        : Integer.parseInt(title.split(" ")[0]);

                requireActivity().getPreferences(Context.MODE_PRIVATE)
                        .edit()
                        .putInt(getString(R.string.saved_pagination_key), chosenPageSize)
                        .apply();

                resetGrid();
                return true;
            });
            pageSizePopup.show();
        });

        backPageButton.setOnClickListener(v -> {
            if(paginationStartIndex > 0) {
                paginationStartIndex = Math.max(paginationStartIndex - currentPageSize, 0);
                pageNumber--;
                pageTracker.setText("Page " + pageNumber);
                setUpGrid();
            }
        });

        nextPageButton.setOnClickListener(v -> {
            if(paginationStartIndex + currentPageSize < numArtifacts) {
                paginationStartIndex = Math.min(paginationStartIndex + currentPageSize, numArtifacts);
                pageNumber++;
                pageTracker.setText("Page " + pageNumber);
                setUpGrid();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpGrid();
    }

    /**
     * Accesses the artifact database, filters the artifacts, and loads the grid of artifacts.
     */
    private void setUpGrid() {
        artifactRepository.getAllArtifacts(new RepositoryCallback<List<Artifact>>() {
            @Override
            public void onSuccess(List<Artifact> artifacts) {
                if (!isAdded()) {
                    return;
                }
                artifacts = filterArtifactsByMode(artifacts);
                artifacts = filterArtifactsBySubstring(artifacts);
                numArtifacts = artifacts.size();
                loadGrid(artifacts);
            }

            @Override
            public void onError(String message) {
                showError("Could not load artifacts", message);
            }
        });
    }

    /**
     * Takes a list of artifacts to load into the grid.
     * Number of artifacts per page can be set using the shared preferences. Key: R.string.saved_pagination_key
     * @param artifacts
     */
    private void loadGrid(List<Artifact> artifacts) {
        int rawPageSize = readPageSizePreference();
        currentPageSize = (rawPageSize == PAGE_SIZE_ALL) ? Math.max(artifacts.size(), 1) : rawPageSize;

        switch (sortMethod) {
            case SORT_NAME_ASC:
                artifacts.sort((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
                break;
            case SORT_NAME_DESC:
                artifacts.sort((a1, a2) -> a2.getName().compareToIgnoreCase(a1.getName()));
                break;
        }

        paginationStartIndex = Math.min(paginationStartIndex, Math.max(artifacts.size() - 1, 0));
        int endIndex = Math.min(paginationStartIndex + currentPageSize, artifacts.size());
        List<Artifact> artifactsToLoad = artifacts.subList(paginationStartIndex, endIndex);
        ArtifactGridAdapter gridAdapter = new ArtifactGridAdapter(artifactsToLoad,
                this::openDetailsScreen);

        rvArtifactGrid.setAdapter(gridAdapter);
    }

    /**
     * @return Pagination saved in shared preferences or 12 if not found.
     */
    private int readPageSizePreference() {
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int defaultPagination = 12;
        String savedPaginationKey = getString(R.string.saved_pagination_key);
        return sharedPref.getInt(savedPaginationKey, defaultPagination);
    }

    /**
     * Resets the grid back to its start state.
     */
    private void resetGrid() {
        paginationStartIndex = 0;
        pageNumber = 1;
        pageTracker.setText("Page " + pageNumber);
        setUpGrid();
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

    /**
     * Filters a list of artifacts by filterMode.
     * @param artifacts list of artifacts to be filtered.
     * @return list of artifacts that has been filtered.
     */
    private List<Artifact> filterArtifactsByMode(List<Artifact> artifacts) {
        if (filterMode == null) {
            return artifacts;
        }

        if (FILTER_ON_DISPLAY.equals(filterMode)) {
            return artifacts.stream().filter(Artifact::isOnDisplay).collect(Collectors.toList());
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : null;

        if (FILTER_LIKED.equals(filterMode)) {
            return artifacts.stream().filter(a -> a.isLikedBy(uid)).collect(Collectors.toList());
        } else if (FILTER_SAVED.equals(filterMode)) {
            return artifacts.stream().filter(a -> a.isSavedBy(uid)).collect(Collectors.toList());
        }
        return artifacts;
    }

    /**
     * Filters a list of artifacts by currentSearch using a substring filter of all the artifact's string fields.
     * @param artifacts the list of artifacts to be filtered.
     * @return a new list of artifacts that has been filtered.
     */
    private List<Artifact> filterArtifactsBySubstring(List<Artifact> artifacts) {
        if(currentSearch == null || currentSearch.isEmpty()) { return artifacts; }

        return artifacts.stream()
                .filter(artifact ->
                        ((artifact.getName().toLowerCase().contains(currentSearch))
                        || (artifact.getDescription().toLowerCase().contains(currentSearch))
                        || (artifact.getCategory().toLowerCase().contains(currentSearch))
                        || (artifact.getDynasty().toLowerCase().contains(currentSearch))
                        || (artifact.getAcqMethod().toLowerCase().contains(currentSearch))
                        || (artifact.getConditionReport().toLowerCase().contains(currentSearch))
                        || (artifact.getLocation().toLowerCase().contains(currentSearch))
                        || (artifact.getNotes().toLowerCase().contains(currentSearch))
                        || (artifact.getOrigin().toLowerCase().contains(currentSearch))
                        || (artifact.getProvenance().toLowerCase().contains(currentSearch))
                        || (artifact.getMaterial().toLowerCase().contains(currentSearch)))
                )
                .collect(Collectors.toList());
    }

    /**
     * Navigates to the expanded details screen.
     * @param artifact the artifact to be viewed.
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
}
