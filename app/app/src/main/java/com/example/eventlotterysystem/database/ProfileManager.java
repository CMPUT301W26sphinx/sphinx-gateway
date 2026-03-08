package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles communication between local user profiles and firebase.
 */
public class ProfileManager {

    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public ProfileManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    /**
     * Saves the desired user into firebase if they do no exist already. If they
     * already exist, updates the information.
     * Note: the userID is the key for the document in firebase.
     * @param user the user profile that you want to save
     */
    public void saveUser(UserProfile user) {

        DocumentReference docRef = usersRef.document(user.getProfileID());
        docRef.set(user);
    }
}
