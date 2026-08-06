package com.golden.geese.model;

/** Manages a user's like relationship with catalogued artifacts */
public interface LikeRepository {
    /** Adds the user's uid to the artifact's likedBy list */
    void likeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    /** Removes the user's uid from the artifact's likedBy list */
    void unlikeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);
}
