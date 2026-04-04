package com.example.eventlotterysystem.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;

/**
 * Helper class to load images stored as Base64 strings in Firestore.
 * Provides caching and scaling to avoid memory issues.
 */
public class ImageHelper {

    private static LruCache<String, Bitmap> sCache;

    static {
        // Use 1/8th of available memory for image cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        sCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // Return size in KB
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * Loads the image from an Event object into an ImageView.
     * Shows a placeholder while loading and an error icon if the image is missing or invalid.
     *
     * @param imageView The target ImageView.
     * @param event     The Event containing image data.
     */
    public static void loadEventImage(ImageView imageView, Event event) {
        if (imageView == null) {
            Log.e("ImageHelper", "ImageView is null");
            return;
        }
        // Show placeholder immediately
        imageView.setImageResource(R.drawable.ic_placeholder);

        if (event == null) {
            Log.e("ImageHelper", "Event is null");
            return;
        }

        if (event.getImageData() == null || event.getImageData().isEmpty()) {
            Log.d("ImageHelper", "No image data for event " + event.getEventId());
            return;
        }

        Log.d("ImageHelper", "Image data length: " + event.getImageData().length());

        String cacheKey = event.getEventId();
        Bitmap cached = sCache.get(cacheKey);
        if (cached != null) {
            Log.d("ImageHelper", "Using cached image for " + cacheKey);
            imageView.setImageBitmap(cached);
            return;
        }

        try {
            byte[] decodedBytes = Base64.decode(event.getImageData(), Base64.DEFAULT);
            Log.d("ImageHelper", "Decoded bytes length: " + decodedBytes.length);
            Bitmap original = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (original == null) {
                Log.e("ImageHelper", "BitmapFactory returned null – invalid Base64?");
                imageView.setImageResource(R.drawable.ic_placeholder);
                return;
            }
            Log.d("ImageHelper", "Bitmap created: " + original.getWidth() + "x" + original.getHeight());

            // Scale to fit ImageView dimensions
            int targetWidth = imageView.getWidth();
            int targetHeight = imageView.getHeight();
            Bitmap scaled = original;
            if (targetWidth > 0 && targetHeight > 0) {
                scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true);
                original.recycle();
            }
            sCache.put(cacheKey, scaled);
            imageView.setImageBitmap(scaled);
        } catch (IllegalArgumentException e) {
            Log.e("ImageHelper", "Base64 decode error", e);
            imageView.setImageResource(R.drawable.ic_placeholder);
        } catch (Exception e) {
            Log.e("ImageHelper", "Unexpected error", e);
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }
}