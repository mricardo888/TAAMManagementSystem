package com.golden.geese.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Uploads and deletes artifact images stored in a Supabase storage bucket.
 * Reads image data from a content {@link Uri}, sends it via HTTP using
 * OkHttp, and reports results back on the main thread through
 */
public class ArtifactImageUploader {
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10MB

    private final String supabaseUrl;
    private final String supabaseAnonKey;
    private final String bucketName;
    private final Context appContext;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    /**
     * Creates an uploader configured from the app's Supabase credentials
     * defined in {@code strings.xml}.
     *
     * @param context any context; the application context is retained to
     *                avoid leaking the passed-in context
     */
    public ArtifactImageUploader(Context context) {
        this.appContext = context.getApplicationContext();
        this.supabaseUrl = context.getString(com.golden.geese.R.string.supabase_url).trim();
        this.supabaseAnonKey = context.getString(com.golden.geese.R.string.supabase_anon_key).trim();
        this.bucketName = context.getString(com.golden.geese.R.string.supabase_image_bucket).trim();
        this.httpClient = new OkHttpClient();

        Looper mainLooper = Looper.getMainLooper();
        this.mainHandler = mainLooper == null ? null : new Handler(mainLooper);
    }

    /**
     * Runs the given action on the main thread, or immediately on the
     * calling thread if no main-thread {@link Handler} is available.
     *
     * @param action the action to run
     */
    private void runOnMainThread(Runnable action) {
        if (mainHandler == null) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    /**
     * Validates and uploads an artifact image to Supabase storage under a
     * path derived from the given lot number, then reports the resulting
     * public URL (or an error) via {@code callback}.
     *
     * @param imageUri  the content URI of the image to upload
     * @param lotNumber the artifact's lot number, used to namespace the
     *                  stored file path
     * @param callback  receives the uploaded image's public URL on
     *                  success, or an error message on failure
     */
    public void uploadArtifactImage(Uri imageUri, String lotNumber, ImageUploadCallback callback) {
        // Validate configuration
        if (!isConfigurationValid()) {
            postError(callback, "Image uploader not configured. Check Supabase credentials in strings.xml");
            return;
        }

        // Validate image file
        String mimeType = appContext.getContentResolver().getType(imageUri);
        if (!isValidImageMimeType(mimeType)) {
            postError(callback, "Please select a valid image file (JPEG, PNG, GIF, WebP)");
            return;
        }

        String extension = getImageExtension(mimeType);
        if (extension == null || extension.isEmpty()) {
            postError(callback, "Unsupported image type");
            return;
        }

        // Read image bytes
        byte[] imageBytes;
        try {
            imageBytes = readImageBytes(imageUri);
        } catch (IOException e) {
            postError(callback, "Failed to read image: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            return;
        }

        // Build file path: artifacts/LOT123/timestamp.jpg
        String filePath = buildFilePath(lotNumber, extension);

        // Upload to Supabase
        performUpload(filePath, mimeType, imageBytes, callback);
    }

    /**
     * Sends the given image bytes to Supabase storage at {@code filePath}
     * and reports the resulting public URL, or an error, via
     * {@code callback}.
     *
     * @param filePath   the destination path within the storage bucket
     * @param mimeType   the image's MIME type, used as the request body's
     *                   content type
     * @param imageBytes the raw image data to upload
     * @param callback   receives the uploaded image's public URL on
     *                   success, or an error message on failure
     */
    private void performUpload(String filePath, String mimeType, byte[] imageBytes, ImageUploadCallback callback) {
        HttpUrl uploadUrl = buildStorageUrl("storage/v1/object", filePath);
        if (uploadUrl == null) {
            postError(callback, "Invalid Supabase URL configuration");
            return;
        }

        RequestBody requestBody = RequestBody.create(imageBytes, MediaType.parse(mimeType));
        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer " + supabaseAnonKey)
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postError(callback, "Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        HttpUrl publicUrl = buildStorageUrl("storage/v1/object/public", filePath);
                        if (publicUrl == null) {
                            postError(callback, "Failed to generate public URL");
                        } else {
                            postSuccess(callback, publicUrl.toString());
                        }
                    } else {
                        postError(callback, "Upload failed with status " + response.code());
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Deletes the artifact image at the given public URL from Supabase
     * storage, then reports success or an error via {@code callback}.
     *
     * @param imageUrl the public URL of the image to delete
     * @param callback receives success or an error message
     */
    public void deleteArtifactImage(String imageUrl, ImageDeleteCallback callback) {
        if (isBlank(imageUrl)) {
            callback.onSuccess();
            return;
        }

        String filePath = extractFilePathFromPublicUrl(imageUrl);
        if (filePath == null) {
            postDeleteError(callback, "Could not resolve storage path from image URL");
            return;
        }

        HttpUrl deleteUrl = buildStorageUrl("storage/v1/object", filePath);
        if (deleteUrl == null) {
            postDeleteError(callback, "Invalid Supabase URL configuration");
            return;
        }

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Authorization", "Bearer " + supabaseAnonKey)
                .delete()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postDeleteError(callback, "Delete failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        postDeleteSuccess(callback);
                    } else {
                        postDeleteError(callback, "Delete failed with status " + response.code());
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Extracts the storage-relative file path from a Supabase public image
     * URL, by locating this uploader's bucket segment within the URL.
     *
     * @param imageUrl the public URL to parse
     * @return the file path within the bucket, or {@code null} if the
     *         expected bucket marker isn't found in the URL
     */
    private String extractFilePathFromPublicUrl(String imageUrl) {
        String marker = "/storage/v1/object/public/" + bucketName + "/";
        int index = imageUrl.indexOf(marker);
        if (index == -1) {
            return null;
        }
        return imageUrl.substring(index + marker.length());
    }

    /**
     * Reports a successful deletion to {@code callback} on the main thread.
     *
     * @param callback the callback to notify
     */
    private void postDeleteSuccess(ImageDeleteCallback callback) {
        runOnMainThread(callback::onSuccess);
    }

    /**
     * Reports a deletion failure to {@code callback} on the main thread.
     *
     * @param callback the callback to notify
     * @param message  a human-readable error message
     */
    private void postDeleteError(ImageDeleteCallback callback, String message) {
        runOnMainThread(() -> callback.onError(message));
    }

    /**
     * Reads the full contents of the image at {@code imageUri} into a byte
     * array, enforcing {@link #MAX_IMAGE_BYTES} as an upper size limit.
     *
     * @param imageUri the content URI of the image to read
     * @return the image's raw bytes
     * @throws IOException if the stream can't be opened, reading fails, or
     *                      the image exceeds the 10MB size limit
     */
    private byte[] readImageBytes(Uri imageUri) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(imageUri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Cannot open image stream");
            }
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                if (outputStream.size() > MAX_IMAGE_BYTES) {
                    throw new IOException("Image exceeds 10MB limit");
                }
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Builds the storage path an artifact image should be uploaded to,
     * namespaced by lot number and timestamped to avoid filename
     * collisions. Characters in the lot number outside
     * [A-Za-z0-9_-] are replaced with underscores.
     *
     * @param lotNumber the artifact's lot number
     * @param extension the file extension to use, without a leading dot
     * @return the storage-relative file path
     */
    private String buildFilePath(String lotNumber, String extension) {
        String safeLotNumber = lotNumber.replaceAll("[^A-Za-z0-9_-]", "_");
        return "artifacts/" + safeLotNumber + "/" + System.currentTimeMillis() + "." + extension;
    }

    /**
     * Builds a full Supabase storage URL by combining the configured base
     * URL, the given storage API path, this uploader's bucket name, and
     * the target file path.
     *
     * @param storagePath the Supabase storage API path segment (e.g.
     *                    {@code "storage/v1/object"})
     * @param filePath    the file path within the bucket
     * @return the resulting URL, or null if invalid
     */
    private HttpUrl buildStorageUrl(String storagePath, String filePath) {
        HttpUrl baseUrl = HttpUrl.parse(supabaseUrl);
        if (baseUrl == null) {
            return null;
        }
        return baseUrl.newBuilder()
                .addPathSegments(storagePath)
                .addPathSegment(bucketName)
                .addPathSegments(filePath)
                .build();
    }

    /**
     * Resolves the file extension associated with a MIME type.
     *
     * @param mimeType the MIME type to resolve
     * @return the corresponding file extension, or {@code null} if
     *         {@code mimeType} is {@code null} or has no known extension
     */
    private String getImageExtension(String mimeType) {
        if (mimeType == null) return null;
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    /**
     * Checks whether a MIME type represents an image.
     *
     * @param mimeType the MIME type to check
     * @return {@code true} if non-null and starts with {@code "image/"}
     */
    private boolean isValidImageMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Checks if this uploader has all the Supabase configuration
     * values (URL, anon key, bucket name) it needs to make requests.
     *
     * @return {@code true} if none of the required configuration values
     *         are blank
     */
    private boolean isConfigurationValid() {
        return !isBlank(supabaseUrl) && !isBlank(supabaseAnonKey) && !isBlank(bucketName);
    }

    /**
     * Reports a successful upload to {@code callback} on the main thread.
     *
     * @param callback the callback to notify
     * @param imageUrl the uploaded image's public URL
     */
    private void postSuccess(ImageUploadCallback callback, String imageUrl) {
        runOnMainThread(() -> callback.onSuccess(imageUrl));
    }

    /**
     * Reports an upload failure to {@code callback} on the main thread.
     *
     * @param callback the callback to notify
     * @param message  a human-readable error message
     */
    private void postError(ImageUploadCallback callback, String message) {
        runOnMainThread(() -> callback.onError(message));
    }

    /**
     * Checks whether a value is {@code null} or contains only whitespace.
     *
     * @param value the value to check
     * @return {@code true} if the value is {@code null} or blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}