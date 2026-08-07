package com.golden.geese.model;

import com.golden.geese.Comment;

import java.util.List;

/** Manages the threaded comments attached to catalogued artifacts */
public interface CommentRepository {
    /** Adds a top-level comment to an artifact's thread */
    void addComment(int lotNum, Comment comment, RepositoryCallback<Void> callback);

    /** Adds a reply nested under an existing comment in an artifact's thread */
    void addReply(int lotNum, String parentCommentId, Comment reply, RepositoryCallback<Void> callback);

    /** Deletes a comment and every reply nested beneath it */
    void deleteComment(int lotNum, String commentId, RepositoryCallback<Void> callback);

    /** Fetches an artifact comments assembled into a reply tree */
    void getComments(int lotNum, RepositoryCallback<List<Comment>> callback);

    /** Counts how many comments the given user has posted across the listed artifacts */
    void getCommentCountByUser(List<Integer> lotNums, String uid, RepositoryCallback<Integer> callback);
}
