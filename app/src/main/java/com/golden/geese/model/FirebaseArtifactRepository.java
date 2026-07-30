package com.golden.geese.model;

import androidx.annotation.NonNull;

import com.golden.geese.Artifact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseArtifactRepository implements ArtifactRepository, LikeRepository, SaveRepository, CommentRepository {
    private static final String ARTIFACTS = "artifacts";
    private static final String ARTIFACT_LIKES = "artifactLikes";
    private static final String ARTIFACT_SAVES = "artifactSaves";
    private static final String COMMENTS = "comments";

    private final DatabaseReference root;

    public FirebaseArtifactRepository() {
        root = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void getAllArtifacts(RepositoryCallback<List<Artifact>> callback) {
        root.child(ARTIFACTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Artifact> artifacts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Artifact artifact = child.getValue(Artifact.class);
                    if (artifact != null) {
                        artifacts.add(artifact);
                    }
                }
                callback.onSuccess(artifacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void addArtifact(Artifact artifact, RepositoryCallback<Void> callback) {
        root.child(ARTIFACTS).child(String.valueOf(artifact.getLotNum()))
                .setValue(artifact)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateArtifact(Artifact artifact, RepositoryCallback<Void> callback) {
        root.child(ARTIFACTS).child(String.valueOf(artifact.getLotNum()))
                .setValue(artifact)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteArtifact(int lotNum, RepositoryCallback<Void> callback) {
        root.child(ARTIFACTS).child(String.valueOf(lotNum))
                .removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void likeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(ARTIFACTS + "/" + lotNum + "/likes", ServerValue.increment(1));
        updates.put(ARTIFACT_LIKES + "/" + lotNum + "/" + uid, true);
        root.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void unlikeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(ARTIFACTS + "/" + lotNum + "/likes", ServerValue.increment(-1));
        updates.put(ARTIFACT_LIKES + "/" + lotNum + "/" + uid, null);
        root.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void isArtifactLikedByUser(int lotNum, String uid, RepositoryCallback<Boolean> callback) {
        root.child(ARTIFACT_LIKES).child(String.valueOf(lotNum)).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onSuccess(snapshot.exists());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        root.child(ARTIFACT_SAVES).child(uid).child(String.valueOf(lotNum))
                .setValue(true)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        root.child(ARTIFACT_SAVES).child(uid).child(String.valueOf(lotNum))
                .removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getSavedLotNumbers(String uid, RepositoryCallback<List<Integer>> callback) {
        root.child(ARTIFACT_SAVES).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Integer> lotNumbers = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            lotNumbers.add(Integer.parseInt(child.getKey()));
                        }
                        callback.onSuccess(lotNumbers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void addComment(int lotNum, Comment comment, RepositoryCallback<Void> callback) {
        root.child(COMMENTS).child(String.valueOf(lotNum)).push()
                .setValue(comment)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteComment(int lotNum, String commentId, RepositoryCallback<Void> callback) {
        root.child(COMMENTS).child(String.valueOf(lotNum)).child(commentId)
                .removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getComments(int lotNum, RepositoryCallback<List<Comment>> callback) {
        root.child(COMMENTS).child(String.valueOf(lotNum))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Comment> comments = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Comment comment = child.getValue(Comment.class);
                            if (comment != null) {
                                comment.setCommentId(child.getKey());
                                comments.add(comment);
                            }
                        }
                        callback.onSuccess(comments);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}
