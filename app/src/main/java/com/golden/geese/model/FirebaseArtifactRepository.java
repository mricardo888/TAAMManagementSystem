package com.golden.geese.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.golden.geese.AdminUser;
import com.golden.geese.Artifact;
import com.golden.geese.Comment;
import com.golden.geese.RegularUser;
import com.golden.geese.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void doesLotNumberExist(int lotNum, RepositoryCallback<Boolean> callback) {
        root.child(ARTIFACTS).child(String.valueOf(lotNum))
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
    public void updateArtifact(Artifact artifact, RepositoryCallback<Void> callback) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", artifact.getName());
        fields.put("description", artifact.getDescription());
        fields.put("category", artifact.getCategory());
        fields.put("material", artifact.getMaterial());
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
    public void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, SAVED_BY, uid, true, callback);
    }

    @Override
    public void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback) {
        updateUidList(lotNum, SAVED_BY, uid, false, callback);
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

    private List<String> readUids(MutableData data) {
        List<String> uids = new ArrayList<>();
        for (MutableData child : data.getChildren()) {
            if (child.getValue() instanceof String) {
                uids.add((String) child.getValue());
            }
        }
        return uids;
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
                            if (child.getKey() != null) {
                                byId.put(child.getKey(), parseComment(child));
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

    @Override
    public void getCommentCountByUser(List<Integer> lotNums, String uid, RepositoryCallback<Integer> callback) {
        if (lotNums.isEmpty()) {
            callback.onSuccess(0);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(lotNums.size());
        AtomicInteger total = new AtomicInteger(0);
        AtomicBoolean failed = new AtomicBoolean(false);

        for (Integer lotNum : lotNums) {
            root.child(COMMENTS).child(String.valueOf(lotNum))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot comment : snapshot.getChildren()) {
                                if (uid != null && uid.equals(comment.child("author").child("uid").getValue(String.class))) {
                                    total.incrementAndGet();
                                }
                            }
                            finishOne();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (failed.compareAndSet(false, true)) {
                                callback.onError(error.getMessage());
                            }
                            finishOne();
                        }

                        private void finishOne() {
                            if (remaining.decrementAndGet() == 0 && !failed.get()) {
                                callback.onSuccess(total.get());
                            }
                        }
                    });
        }
    }

    private Comment parseComment(DataSnapshot child) {
        String text = child.child("text").getValue(String.class);
        Comment comment = new Comment(parseAuthor(child.child("author")), text != null ? text : "");
        comment.setCommentId(child.getKey());
        comment.setParentId(child.child("parentId").getValue(String.class));

        LocalDateTime timestamp = parseTimestamp(child.child("timestamp"));
        if (timestamp != null) {
            comment.setTimestamp(timestamp);
        }

        comment.setEdited(Boolean.TRUE.equals(child.child("edited").getValue(Boolean.class)));

        return comment;
    }

    private User parseAuthor(DataSnapshot authorSnapshot) {
        boolean isAdmin = Boolean.TRUE.equals(authorSnapshot.child("admin").getValue(Boolean.class));
        String username = authorSnapshot.child("username").getValue(String.class);
        String pfp = authorSnapshot.child("pfp").getValue(String.class);

        User author = isAdmin ? new AdminUser(username, pfp) : new RegularUser(username, pfp);
        author.setUid(authorSnapshot.child("uid").getValue(String.class));
        author.setEmail(authorSnapshot.child("email").getValue(String.class));
        return author;
    }

    private LocalDateTime parseTimestamp(DataSnapshot timestampSnapshot) {
        Integer year = timestampSnapshot.child("year").getValue(Integer.class);
        Integer month = timestampSnapshot.child("monthValue").getValue(Integer.class);
        Integer day = timestampSnapshot.child("dayOfMonth").getValue(Integer.class);
        if (year == null || month == null || day == null) {
            return null;
        }

        Integer hour = timestampSnapshot.child("hour").getValue(Integer.class);
        Integer minute = timestampSnapshot.child("minute").getValue(Integer.class);
        Integer second = timestampSnapshot.child("second").getValue(Integer.class);
        Integer nano = timestampSnapshot.child("nano").getValue(Integer.class);

        return LocalDateTime.of(year, month, day,
                hour != null ? hour : 0,
                minute != null ? minute : 0,
                second != null ? second : 0,
                nano != null ? nano : 0);
    }

    private List<Comment> buildReplyTree(Map<String, Comment> byId) {
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : byId.values()) {
            Comment parent = comment.isReply() ? byId.get(comment.getParentId()) : null;
            if (parent == null) {
                roots.add(comment);
            } else {
                parent.getReplies().add(comment);
            }
        }
        return roots;
    }
}