package com.golden.geese.model;

public interface SaveRepository {
    void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);
}
