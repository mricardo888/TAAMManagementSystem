package com.golden.geese.model;

/**
 * Callback for asynchronous repository operations
 *
 * @param <T> the type of result returned on success
 */
public interface RepositoryCallback<T> {
    /** Invoked with the result once the operation completes successfully */
    void onSuccess(T result);

    /** Invoked with a human-readable message if the operation fails */
    void onError(String message);
}
