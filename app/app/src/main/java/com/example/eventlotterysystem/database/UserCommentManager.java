package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages stored event comments
 */
public class UserCommentManager {

    private FirebaseFirestore db;
    private CollectionReference eventRef;

    private UserCommentManager() {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
    }

    private static class Holder {
        private static final UserCommentManager instance = new UserCommentManager();
    }

    public static UserCommentManager getInstance() {
        return Holder.instance;
    }

    /**
     * Adds the comment to the comments subcollection of an event
     *
     * @param eventID
     * @param comment
     */
    public void addCommentToEvent(String eventID, String comment) {
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
            eventRef.document(eventID).collection("comments").add(data);
        });


    }

    public interface UserCommentCallback {
        void onCommentLoaded(List<UserComment> comments);

        void onError(Exception e);
    }

    // TODO: implement a function to get all comments from an event
    public void getCommentsFromEvent(String eventID, UserCommentCallback callback) {
        eventRef.document(eventID).collection("comments").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(snapshot -> {
            List<UserComment> comments = snapshot.toObjects(UserComment.class);
            if (comments == null) comments = new ArrayList<>();
            callback.onCommentLoaded(comments);
        }).addOnFailureListener(callback::onError);

    }
}
