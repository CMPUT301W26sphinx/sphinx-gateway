package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserCommentManager {
    /**
     * This class manages stored event comments
     */

    private FirebaseFirestore db;
    private CollectionReference eventRef;

    // TODO: make this singleton
    public UserCommentManager() {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
    }

    // TODO: add comment to an event
    public void addCommentToEvent(String eventID, UserComment comment){
        ProfileManager manager = ProfileManager.getInstance();
        // get userID
        String uid = manager.getUserID();
        // get the user
        manager.getUserProfile(user -> {
           String firstName = user.getFirstName();
            // set fields
            Map<String, Object> data = new HashMap<>();
            data.put("text", comment.getText());
            data.put("userID", uid);
            data.put("userName", firstName);
            data.put("timestamp", FieldValue.serverTimestamp());
            eventRef.document(eventID).collection("comments").add(data);
        });


    }
}
