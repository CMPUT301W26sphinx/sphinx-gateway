package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
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
}
