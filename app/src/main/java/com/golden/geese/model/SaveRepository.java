package com.golden.geese.model;

/** Manages a user's saved/bookmarked relationship with catalogued artifacts */
public interface SaveRepository {
    /** Adds the user's uid to the artifact savedBy list */
    void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    /** Removes the user's uid from the artifact savedBy list */
    void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);
}
