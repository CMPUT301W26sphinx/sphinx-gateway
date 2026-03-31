package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

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
    public void addCommentToEvent(String eventID, String comment, OnCommentAddedListener listener) {
        ProfileManager manager = ProfileManager.getInstance();
        // get userID
        String uid = manager.getUserID();
        // get the user
        manager.getUserProfile(user -> {
            String firstName = user.getFirstName();
            // set fields
            Map<String, Object> data = new HashMap<>();
            data.put("text", comment);
            data.put("userID", uid);
            data.put("userName", firstName);
            data.put("timestamp", FieldValue.serverTimestamp());
            eventRef.document(eventID).collection("comments").add(data).addOnSuccessListener(listener::onSuccess).addOnFailureListener(listener::onFailure);
        });


    }

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
    public void getCommentsFromEvent(String eventID, UserCommentCallback callback) {
        eventRef.document(eventID).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(snapshot -> {
            List<UserComment> comments = snapshot.toObjects(UserComment.class);
            callback.onCommentLoaded(comments);
        }).addOnFailureListener(callback::onError);

    }

    /**
     * Listens to the comments subcollection and sends updates when changed
     *
     * @param eventID
     * @param callback
     * @return
     */
    public ListenerRegistration listenToComments(String eventID, UserCommentCallback callback) {
        return eventRef.document(eventID).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((snapshot, error) -> {
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

    // TODO: add a delete comment for a given event function
    public interface DeleteCommentListener {
        void onSuccess(DocumentReference docRef);

        void onFailure(Exception e);
    }

    public interface OnCommentDeletedListener {
        void onSuccess(Task<Void> docRef);

        void onFailure(Exception e);
    }

    public void deleteComment(String eventID, String commentID, OnCommentDeletedListener listener) {
       listener.onSuccess(eventRef.document(eventID).collection("comments").document(commentID).delete());
    }

}
