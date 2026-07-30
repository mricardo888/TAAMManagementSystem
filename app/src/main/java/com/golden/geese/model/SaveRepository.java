package com.golden.geese.model;

import java.util.List;

public interface SaveRepository {
    void saveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void unsaveArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void getSavedLotNumbers(String uid, RepositoryCallback<List<Integer>> callback);
}
