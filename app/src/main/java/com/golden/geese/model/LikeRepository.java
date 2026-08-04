package com.golden.geese.model;

public interface LikeRepository {
    void likeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void unlikeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);
}
