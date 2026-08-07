package com.golden.geese.model;

import com.golden.geese.Artifact;

import java.util.List;

/** Manages the /artifacts catalog records */
public interface ArtifactRepository {
    /** Fetches every catalogued artifact */
    void getAllArtifacts(RepositoryCallback<List<Artifact>> callback);

    /** Writes a new artifact, keyed by its lot number */
    void addArtifact(Artifact artifact, RepositoryCallback<Void> callback);

    /** Resolves whether an artifact already exists for the given lot number */
    void doesLotNumberExist(int lotNum, RepositoryCallback<Boolean> callback);

    /** Updates an existing artifact's catalog fields */
    void updateArtifact(Artifact artifact, RepositoryCallback<Void> callback);

    /** Deletes an artifact and its associated comment thread */
    void deleteArtifact(int lotNum, RepositoryCallback<Void> callback);
}
