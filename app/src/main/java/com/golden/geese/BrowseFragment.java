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

public class BrowseFragment extends Fragment {
    private static final int PAGE_SIZE_ALL = -1;

    private static final String SORT_NAME_ASC = "Name (A-Z)";
    private static final String SORT_NAME_DESC = "Name (Z-A)";

    private final ArtifactRepository artifactRepository = new FirebaseArtifactRepository();
    private RecyclerView rvArtifactGrid;
    private TextView pageTracker;
    private int paginationStartIndex = 0;
    private int numArtifacts = 0;
    private int currentPageSize = 12;
    private int pageNumber = 1;
    private String sortMethod = SORT_NAME_ASC;
    private String currentSearch = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    private void setUpGrid() {
        artifactRepository.getAllArtifacts(new RepositoryCallback<List<Artifact>>() {
            @Override
            public void onSuccess(List<Artifact> artifacts) {
                if (!isAdded()) {
                    return;
                }
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

    private int readPageSizePreference() {
        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int defaultPagination = 12;
        String savedPaginationKey = getString(R.string.saved_pagination_key);
        return sharedPref.getInt(savedPaginationKey, defaultPagination);
    }

    private void resetGrid() {
        paginationStartIndex = 0;
        pageNumber = 1;
        pageTracker.setText("Page " + pageNumber);
        setUpGrid();
    }

    private void showError(String what, String reason) {
        if (!isAdded()) {
            return;
        }
        String detail = (reason == null || reason.trim().isEmpty()) ? "" : ": " + reason;
        Toast.makeText(requireContext(), what + detail, Toast.LENGTH_SHORT).show();
    }

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
