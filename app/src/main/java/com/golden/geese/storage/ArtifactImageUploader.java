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

public class ArtifactImageUploader {
    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10MB

    private final String supabaseUrl;
    private final String supabaseAnonKey;
    private final String bucketName;
    private final Context appContext;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public ArtifactImageUploader(Context context) {
        this.appContext = context.getApplicationContext();
        this.supabaseUrl = context.getString(com.golden.geese.R.string.supabase_url).trim();
        this.supabaseAnonKey = context.getString(com.golden.geese.R.string.supabase_anon_key).trim();
        this.bucketName = context.getString(com.golden.geese.R.string.supabase_image_bucket).trim();
        this.httpClient = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

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

    private String buildFilePath(String lotNumber, String extension) {
        String safeLotNumber = lotNumber.replaceAll("[^A-Za-z0-9_-]", "_");
        return "artifacts/" + safeLotNumber + "/" + System.currentTimeMillis() + "." + extension;
    }

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

    private String getImageExtension(String mimeType) {
        if (mimeType == null) return null;
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    private boolean isValidImageMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    private boolean isConfigurationValid() {
        return !isBlank(supabaseUrl) && !isBlank(supabaseAnonKey) && !isBlank(bucketName);
    }

    private void postSuccess(ImageUploadCallback callback, String imageUrl) {
        mainHandler.post(() -> callback.onSuccess(imageUrl));
    }

    private void postError(ImageUploadCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
