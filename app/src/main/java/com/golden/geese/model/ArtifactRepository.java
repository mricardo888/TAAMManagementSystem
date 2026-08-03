package com.golden.geese.model;

import com.golden.geese.Artifact;

import java.util.List;

public interface ArtifactRepository {
    void getAllArtifacts(RepositoryCallback<List<Artifact>> callback);

    void addArtifact(Artifact artifact, RepositoryCallback<Void> callback);

    void doesLotNumberExist(int lotNum, RepositoryCallback<Boolean> callback);

    void updateArtifact(Artifact artifact, RepositoryCallback<Void> callback);

    void deleteArtifact(int lotNum, RepositoryCallback<Void> callback);
}
