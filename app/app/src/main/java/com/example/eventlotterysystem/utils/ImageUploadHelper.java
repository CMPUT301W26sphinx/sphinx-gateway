package com.example.eventlotterysystem.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class to pick an image, compress it (to stay under a size limit),
 * and convert to Base64.
 */
public class ImageUploadHelper {

    public interface ImageUploadCallback {
        void onImageLoaded(String base64Image);
        void onError(String error);
    }

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageUploadCallback callback;

    // Configurable parameters
    private int maxDimension = 800;      // pixels (largest side)
    private int initialQuality = 80;     // JPEG quality (0-100)
    private int maxTargetKB = 290;        // final Base64 size target (kilobytes)
    private int maxAttempts = 5;          // number of compression attempts

    public ImageUploadHelper(AppCompatActivity activity) {
        this.activity = activity;
        pickImageLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            processImage(imageUri);
                        } else {
                            if (callback != null) callback.onError("No image selected");
                        }
                    } else {
                        if (callback != null) callback.onError("Image selection cancelled");
                    }
                }
        );
    }

    /**
     * Start the image picker.
     */
    public void pickImage(ImageUploadCallback callback) {
        this.callback = callback;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Process the selected image: downsample, compress, and encode to Base64.
     * Repeatedly reduces quality (and optionally dimensions) until the Base64 string
     * is under the target size.
     */
    private void processImage(Uri imageUri) {
        try {
            // First, load just the bounds to determine original dimensions
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate sample size to bring the largest dimension down to maxDimension
            int sampleSize = 1;
            int maxOriginal = Math.max(options.outWidth, options.outHeight);
            if (maxOriginal > maxDimension) {
                sampleSize = maxOriginal / maxDimension;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            // Decode with sampling
            inputStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap == null) {
                if (callback != null) callback.onError("Failed to decode image");
                return;
            }

            // Try to compress until size is acceptable
            int quality = initialQuality;
            byte[] imageBytes = null;
            int attempt = 0;
            while (attempt < maxAttempts) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageBytes = baos.toByteArray();
                int sizeKB = imageBytes.length / 1024;
                if (sizeKB <= maxTargetKB) {
                    break; // size acceptable
                }
                // If still too large, reduce quality
                quality = Math.max(quality - 15, 30);
                attempt++;
            }

            // If still too large after all attempts, reduce dimensions further and try again
            if (imageBytes.length / 1024 > maxTargetKB) {
                // Scale down by 0.7 factor
                int newWidth = (int) (bitmap.getWidth() * 0.7);
                int newHeight = (int) (bitmap.getHeight() * 0.7);
                if (newWidth > 0 && newHeight > 0) {
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    bitmap.recycle();
                    bitmap = scaled;
                    // Retry compression with initial quality
                    quality = initialQuality;
                    for (int i = 0; i < maxAttempts; i++) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                        imageBytes = baos.toByteArray();
                        if (imageBytes.length / 1024 <= maxTargetKB) break;
                        quality = Math.max(quality - 15, 30);
                    }
                }
            }

            // Encode to Base64
            String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            if (callback != null) callback.onImageLoaded(base64);

            // Clean up
            bitmap.recycle();

        } catch (IOException e) {
            if (callback != null) callback.onError("Failed to read image: " + e.getMessage());
        } catch (OutOfMemoryError e) {
            if (callback != null) callback.onError("Image too large to process");
        }
    }

    // Optional setters to adjust compression parameters
    public void setMaxDimension(int maxDimension) { this.maxDimension = maxDimension; }
    public void setInitialQuality(int initialQuality) { this.initialQuality = initialQuality; }
    public void setMaxTargetKB(int maxTargetKB) { this.maxTargetKB = maxTargetKB; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
}