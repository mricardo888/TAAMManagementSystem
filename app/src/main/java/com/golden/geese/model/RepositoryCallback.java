package com.golden.geese.model;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
