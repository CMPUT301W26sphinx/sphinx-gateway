package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserCommentManager {
    /**
     * This class manages stored event comments
     */

    private FirebaseFirestore db;
    private CollectionReference eventRef;

    public UserCommentManager() {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
    }

    // TODO: add comment to an event
    public void addCommentToEvent(String eventID, UserComment comment){
        // get user ID
        String uid = ProfileManager.getInstance().getUserID();
        eventRef.document(eventID).collection("comments").document(uid).set(comment);
    }
}
