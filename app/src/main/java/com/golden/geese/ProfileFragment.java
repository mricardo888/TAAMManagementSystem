package com.golden.geese;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.golden.geese.model.FirebaseArtifactRepository;
import com.golden.geese.model.RepositoryCallback;
import com.golden.geese.ui.UserSettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ProfileFragment extends Fragment {
    private final FirebaseArtifactRepository repository = new FirebaseArtifactRepository();

    private RecyclerView likedArtifactsRV;
    private RecyclerView savedArtifactsRV;
    private ImageButton settingsButton;
    private TextView profileName;
    private TextView likesCounter;
    private TextView commentsCounter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsButton = view.findViewById(R.id.settings_button);

        likedArtifactsRV = view.findViewById(R.id.liked_artifact_scroller);

        savedArtifactsRV = view.findViewById(R.id.saved_artifact_scroller);

        likedArtifactsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        savedArtifactsRV.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        view.findViewById(R.id.tab_home).setOnClickListener(v -> {
            loadFragment(new HomeFragment());
        });

        settingsButton.setOnClickListener( v -> {
            loadFragment(new UserSettingsFragment());
        });

        view.findViewById(R.id.liked_view_all_button).setOnClickListener(v -> {
            loadFragment(BrowseFragment.newInstance(BrowseFragment.FILTER_LIKED));
        });

        view.findViewById(R.id.saved_view_all_button).setOnClickListener(v -> {
            loadFragment(BrowseFragment.newInstance(BrowseFragment.FILTER_SAVED));
        });

        profileName = view.findViewById(R.id.profile_name);
        likesCounter = view.findViewById(R.id.likes_counter);
        commentsCounter = view.findViewById(R.id.comments_counter);
    }

    @Override
    public void onResume() {
        super.onResume();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileName.setText(currentUser.getUsername());
        }

        loadArtifacts();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadArtifacts() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid() == null) {
            return;
        }
        String uid = currentUser.getUid();

        repository.getAllArtifacts(new RepositoryCallback<List<Artifact>>() {
            @Override
            public void onSuccess(List<Artifact> allArtifacts) {
                if (!isAdded()) {
                    return;
                }
                List<Artifact> liked = filter(allArtifacts, a -> a.isLikedBy(uid));
                likedArtifactsRV.setAdapter(adapterFor(liked));
                savedArtifactsRV.setAdapter(adapterFor(filter(allArtifacts, a -> a.isSavedBy(uid))));
                likesCounter.setText(String.valueOf(liked.size()));

                List<Integer> lotNums = new ArrayList<>();
                for (Artifact artifact : allArtifacts) {
                    lotNums.add(artifact.getLotNum());
                }
                loadCommentCount(lotNums, uid);
            }

            @Override
            public void onError(String message) {
                showError("Could not load artifacts", message);
            }
        });
    }

    private void loadCommentCount(List<Integer> lotNums, String uid) {
        repository.getCommentCountByUser(lotNums, uid, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (!isAdded()) {
                    return;
                }
                commentsCounter.setText(String.valueOf(count));
            }

            @Override
            public void onError(String message) {
                showError("Could not load comment count", message);
            }
        });
    }

    private ArtifactAdapter adapterFor(List<Artifact> artifacts) {
        return new ArtifactAdapter(artifacts, R.layout.item_artifact, this::openDetailsScreen);
    }

    private void openDetailsScreen(Artifact artifact) {
        Bundle args = new Bundle();
        args.putSerializable("Artifact", artifact);

        ExpandedArtifactFragment fragment = new ExpandedArtifactFragment();
        fragment.setArguments(args);

        loadFragment(fragment);
    }

    private List<Artifact> filter(List<Artifact> artifacts, Predicate<Artifact> keep) {
        List<Artifact> filtered = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            if (keep.test(artifact)) {
                filtered.add(artifact);
            }
        }
        return filtered;
    }

    private void showError(String what, String reason) {
        if (!isAdded()) {
            return;
        }
        String detail = (reason == null || reason.trim().isEmpty()) ? "" : ": " + reason;
        Toast.makeText(requireContext(), what + detail, Toast.LENGTH_SHORT).show();
    }
}
