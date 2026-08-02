package com.golden.geese.model;

import java.util.List;

public interface LikeRepository {
    void likeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void unlikeArtifact(int lotNum, String uid, RepositoryCallback<Void> callback);

    void isArtifactLikedByUser(int lotNum, String uid, RepositoryCallback<Boolean> callback);

    void getLikeCount(int lotNum, RepositoryCallback<Integer> callback);

    void getLikedLotNumbers(String uid, RepositoryCallback<List<Integer>> callback);
}
