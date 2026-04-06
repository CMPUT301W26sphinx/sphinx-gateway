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

/**
 * This class manages stored event comments
 *
 */
public class UserCommentManager {
    // https://firebase.google.com/docs/firestore/query-data/listen
    // See for reference for the implementation of this class
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

    /**
     * Adds the comment to the comments subcollection of an event
     *
     * @param eventID
     * @param comment
     */
    // Main branch version (with isOrganizer)
    public void addCommentToEvent(String eventID, String comment, boolean isOrganizer, OnCommentAddedListener listener) {
        ProfileManager manager = ProfileManager.getInstance();
        // get userID
        String uid = manager.getUserID();
        // get the user
        manager.getUserProfile(user -> {
            String firstName = user.getFirstName();
            DocumentReference docRef = eventRef.document(eventID).collection("comments").document();
            String commentID = docRef.getId();
            // set fields
            Map<String, Object> data = new HashMap<>();
            data.put("text", comment);
            data.put("userID", uid);
            data.put("isOrganizer", isOrganizer);
            data.put("userName", firstName);
            data.put("timestamp", FieldValue.serverTimestamp());
            data.put("commentID", commentID);
            // save document to firestore
            docRef.set(data)
                    .addOnSuccessListener(aVoid -> listener.onSuccess(docRef))
                    .addOnFailureListener(listener::onFailure);
        });
    }

    // Optional: convenience overload without isOrganizer (default false)
    public void addCommentToEvent(String eventID, String comment, OnCommentAddedListener listener) {
        addCommentToEvent(eventID, comment, false, listener);
    }

    // ------------------------------------------------------------------------
    // Get comments
    // ------------------------------------------------------------------------
    public interface UserCommentCallback {
        void onCommentLoaded(List<UserComment> comments);
        void onError(Exception e);
    }

    /**
     * Get the comments from an event, sorted by descending date
     *
     * @param eventID
     * @param callback
     */

    // Main branch method: uses automatic mapping (commentID field is present)
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

    // Admin branch method: also sets the document ID as commentId (redundant but safe)
    public void getCommentsFromEventWithIds(String eventID, UserCommentCallback callback) {
        eventRef.document(eventID).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserComment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        UserComment comment = doc.toObject(UserComment.class);
                        comment.setCommentID(doc.getId()); // ensure ID is set (already in Firestore but safe)
                        comments.add(comment);
                    }
                    callback.onCommentLoaded(comments);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Listens to the comments subcollection and sends the updated list when changed
     *
     * @param eventID
     * @param callback
     * @return
     */
    // ------------------------------------------------------------------------
    // Realtime listener
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

    public void deleteComment(String eventID, String commentID, OnCommentDeletedListener listener) {
        eventRef.document(eventID).collection("comments").document(commentID)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }
}