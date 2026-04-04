package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCommentManager {

    private final CollectionReference eventRef;

    private UserCommentManager() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
    }

    private static class Holder {
        private static final UserCommentManager instance = new UserCommentManager();
    }

    public static UserCommentManager getInstance() {
        return Holder.instance;
    }

    // ------------------------------------------------------------------------
    // Add comment
    // ------------------------------------------------------------------------
    public interface OnCommentAddedListener {
        void onSuccess(DocumentReference docRef);
        void onFailure(Exception e);
    }

    public void addCommentToEvent(String eventID, String comment, OnCommentAddedListener listener) {
        ProfileManager manager = ProfileManager.getInstance();
        String uid = manager.getUserID();
        manager.getUserProfile(user -> {
            String firstName = user.getFirstName();
            Map<String, Object> data = new HashMap<>();
            data.put("text", comment);
            data.put("userID", uid);
            data.put("userName", firstName);
            data.put("timestamp", FieldValue.serverTimestamp());
            eventRef.document(eventID).collection("comments")
                    .add(data)
                    .addOnSuccessListener(listener::onSuccess)
                    .addOnFailureListener(listener::onFailure);
        });
    }

    // ------------------------------------------------------------------------
    // Get comments (with or without document IDs)
    // ------------------------------------------------------------------------
    public interface UserCommentCallback {
        void onCommentLoaded(List<UserComment> comments);
        void onError(Exception e);
    }

    /**
     * Gets comments WITHOUT document IDs (simple toObject).
     * Useful for realtime listeners.
     */
    public void getCommentsFromEvent(String eventID, UserCommentCallback callback) {
        eventRef.document(eventID).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserComment> comments = snapshot.toObjects(UserComment.class);
                    callback.onCommentLoaded(comments);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Gets comments WITH Firestore document IDs (populates commentId field).
     * Needed for deletion.
     */
    public void getCommentsFromEventWithIds(String eventID, UserCommentCallback callback) {
        eventRef.document(eventID).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserComment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        UserComment comment = doc.toObject(UserComment.class);
                        comment.setCommentId(doc.getId());
                        comments.add(comment);
                    }
                    callback.onCommentLoaded(comments);
                })
                .addOnFailureListener(callback::onError);
    }

    // ------------------------------------------------------------------------
    // Realtime listener (optional)
    // ------------------------------------------------------------------------
    public ListenerRegistration listenToComments(String eventID, UserCommentCallback callback) {
        return eventRef.document(eventID).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (snapshot != null) {
                        List<UserComment> comments = snapshot.toObjects(UserComment.class);
                        callback.onCommentLoaded(comments);
                    }
                });
    }

    // ------------------------------------------------------------------------
    // Delete comment
    // ------------------------------------------------------------------------
    public interface OnCommentDeletedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void deleteComment(String eventID, String commentId, OnCommentDeletedListener listener) {
        eventRef.document(eventID).collection("comments").document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }
}