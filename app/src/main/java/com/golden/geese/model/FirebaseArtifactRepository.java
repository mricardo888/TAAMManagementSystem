package com.golden.geese.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.golden.geese.Artifact;
import com.golden.geese.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirebaseArtifactRepository implements ArtifactRepository, LikeRepository, SaveRepository, CommentRepository {
    private static final String ARTIFACTS = "artifacts";
    private static final String COMMENTS = "comments";
    private static final String LIKED_BY = "likedBy";
    private static final String SAVED_BY = "savedBy";

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
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", artifact.getName());
        fields.put("description", artifact.getDescription());
        fields.put("category", artifact.getCategory());
        fields.put("materials", artifact.getMaterials());
        fields.put("dynasty", artifact.getDynasty());
        fields.put("origin", artifact.getOrigin());
        fields.put("dimensions", artifact.getDimensions());
        fields.put("conditionReport", artifact.getConditionReport());
        fields.put("location", artifact.getLocation());
        fields.put("acqMethod", artifact.getAcqMethod());
        fields.put("provenance", artifact.getProvenance());
        fields.put("accessionNum", artifact.getAccessionNum());
        fields.put("notes", artifact.getNotes());
        fields.put("image", artifact.getImage());

        root.child(ARTIFACTS).child(String.valueOf(artifact.getLotNum()))
                .updateChildren(fields)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteArtifact(int lotNum, RepositoryCallback<Void> callback) {
        Map<String, Object> deletions = new HashMap<>();
        deletions.put(ARTIFACTS + "/" + lotNum, null);
        deletions.put(COMMENTS + "/" + lotNum, null);
        root.updateChildren(deletions)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void likeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, LIKED_BY, uid, true, callback);
    }

    @Override
    public void unlikeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, LIKED_BY, uid, false, callback);
    }

    @Override
    public void isArtifactLikedByUser(int lotNum, String uid, RepositoryCallback<Boolean> callback) {
        containsUid(lotNum, LIKED_BY, uid, callback);
    }

    @Override
    public void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, SAVED_BY, uid, true, callback);
    }

    @Override
    public void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, SAVED_BY, uid, false, callback);
    }

    @Override
    public void isArtifactSavedByUser(int lotNum, String uid, RepositoryCallback<Boolean> callback) {
        containsUid(lotNum, SAVED_BY, uid, callback);
    }

    @Override
    public void getSavedLotNumbers(String uid, RepositoryCallback<List<Integer>> callback) {
        root.child(ARTIFACTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> lotNumbers = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (readUids(child.child(SAVED_BY)).contains(uid)) {
                        Integer lotNum = parseLotNum(child.getKey());
                        if (lotNum != null) {
                            lotNumbers.add(lotNum);
                        }
                    }
                }
                callback.onSuccess(lotNumbers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void updateUidList(int lotNum, String listName, String uid, boolean present,
                               RepositoryCallback<Void> callback) {
        root.child(ARTIFACTS).child(String.valueOf(lotNum)).child(listName)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        List<String> uids = readUids(currentData);
                        if (present) {
                            if (!uids.contains(uid)) {
                                uids.add(uid);
                            }
                        } else {
                            uids.remove(uid);
                        }
                        currentData.setValue(uids.isEmpty() ? null : uids);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed,
                                           @Nullable DataSnapshot snapshot) {
                        if (error != null) {
                            callback.onError(error.getMessage());
                        } else if (!committed) {
                            callback.onError("Could not update " + listName + ". Please try again.");
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                });
    }

    private void containsUid(int lotNum, String listName, String uid, RepositoryCallback<Boolean> callback) {
        root.child(ARTIFACTS).child(String.valueOf(lotNum)).child(listName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onSuccess(readUids(snapshot).contains(uid));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private List<String> readUids(MutableData data) {
        List<String> uids = new ArrayList<>();
        for (MutableData child : data.getChildren()) {
            addIfString(child.getValue(), uids);
        }
        return uids;
    }

    private List<String> readUids(DataSnapshot data) {
        List<String> uids = new ArrayList<>();
        for (DataSnapshot child : data.getChildren()) {
            addIfString(child.getValue(), uids);
        }
        return uids;
    }

    private void addIfString(Object value, List<String> uids) {
        if (value instanceof String) {
            uids.add((String) value);
        }
    }

    private Integer parseLotNum(String key) {
        try {
            return Integer.valueOf(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void addComment(int lotNum, Comment comment, RepositoryCallback<Void> callback) {
        writeComment(lotNum, null, comment, callback);
    }

    @Override
    public void addReply(int lotNum, String parentCommentId, Comment reply, RepositoryCallback<Void> callback) {
        if (parentCommentId == null || parentCommentId.isEmpty()) {
            callback.onError("A reply needs the id of the comment it replies to.");
            return;
        }
        writeComment(lotNum, parentCommentId, reply, callback);
    }

    private void writeComment(int lotNum, String parentId, Comment comment, RepositoryCallback<Void> callback) {
        comment.setParentId(parentId);
        DatabaseReference commentRef = root.child(COMMENTS).child(String.valueOf(lotNum)).push();
        commentRef.setValue(comment)
                .addOnSuccessListener(unused -> {
                    comment.setCommentId(commentRef.getKey());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteComment(int lotNum, String commentId, RepositoryCallback<Void> callback) {
        DatabaseReference threadRef = root.child(COMMENTS).child(String.valueOf(lotNum));
        threadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> deletions = new HashMap<>();
                collectSubtree(snapshot, commentId, deletions);
                threadRef.updateChildren(deletions)
                        .addOnSuccessListener(unused -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void collectSubtree(DataSnapshot thread, String commentId, Map<String, Object> deletions) {
        deletions.put(commentId, null);
        for (DataSnapshot child : thread.getChildren()) {
            String key = child.getKey();
            if (key == null || deletions.containsKey(key)) {
                continue;
            }
            if (commentId.equals(child.child("parentId").getValue(String.class))) {
                collectSubtree(thread, key, deletions);
            }
        }
    }

    @Override
    public void getComments(int lotNum, RepositoryCallback<List<Comment>> callback) {
        root.child(COMMENTS).child(String.valueOf(lotNum))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Comment> byId = new LinkedHashMap<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Comment comment = child.getValue(Comment.class);
                            if (comment != null && child.getKey() != null) {
                                comment.setCommentId(child.getKey());
                                //comment.getReplies().clear();
                                byId.put(child.getKey(), comment);
                            }
                        }
                        callback.onSuccess(buildReplyTree(byId));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private List<Comment> buildReplyTree(Map<String, Comment> byId) {
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : byId.values()) {
            Comment parent = comment.isReply() ? byId.get(comment.getParentId()) : null;
            if (parent == null) {
                roots.add(comment);
            } else {
                parent.getSubComments().add(comment);
            }
        }
        return roots;
    }
}
