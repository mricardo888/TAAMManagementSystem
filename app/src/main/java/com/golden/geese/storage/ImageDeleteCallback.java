package com.golden.geese.storage;

public interface ImageDeleteCallback {
    /**
     * Callback for reporting the result of an artifact image deletion.
     */
    void onSuccess();
    /**
     * Called when the image deletion fails.
     *
     * @param errorMessage a description of the failure
     */
    void onError(String errorMessage);
}
