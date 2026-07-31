package com.golden.geese.model;

import com.golden.geese.Comment;

import java.util.List;
import com.golden.geese.Comment;

public interface CommentRepository {
    void addComment(int lotNum, Comment comment, RepositoryCallback<Void> callback);

    void addReply(int lotNum, String parentCommentId, Comment reply, RepositoryCallback<Void> callback);

    void deleteComment(int lotNum, String commentId, RepositoryCallback<Void> callback);

    void getComments(int lotNum, RepositoryCallback<List<Comment>> callback);
}
