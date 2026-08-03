package com.golden.geese.storage;

public interface ImageDeleteCallback {
    void onSuccess();
    void onError(String errorMessage);
}
