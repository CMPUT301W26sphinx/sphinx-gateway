package com.example.eventlotterysystem.database;

import android.util.Log;

import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.model.ImageItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventImageRepository implements ImageRepository {
    private static final String TAG = "EventImageRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void getAllImages(ImagesCallback callback) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ImageItem> images = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String id = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("description");
                            String imageData = doc.getString("imageData");
                            String uploaderName = "System";

                            ImageItem item = new ImageItem(id, title, description, "placeholder", uploaderName, imageData);
                            images.add(item);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing event document", e);
                        }
                    }
                    callback.onImagesLoaded(images);
                })
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void deleteImage(String imageId, DeleteCallback callback) {
        // Delete the entire event document
        db.collection("events").document(imageId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    /**
     * Removes only the imageData field from the event document.
     */
    public void removeImageData(String eventId, DeleteCallback callback) {
        db.collection("events").document(eventId)
                .update("imageData", null)  // set field to null
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}