package com.golden.geese.storage;

public interface ImageUploadCallback {
    void onSuccess(String imageUrl);
    void onError(String errorMessage);
}
