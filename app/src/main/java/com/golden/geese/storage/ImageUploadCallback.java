package com.golden.geese.storage;

public interface ImageUploadCallback {
    /**
     * Callback for reporting the result of a successful artifact image upload
     */
    void onSuccess(String imageUrl);

    /**
     * Called when the image upload fails.
     *
     * @param errorMessage a description of the failure
     */
    void onError(String errorMessage);
}
