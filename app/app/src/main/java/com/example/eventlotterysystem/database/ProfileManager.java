package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Handles communication between local user profiles and firebase.
 */
public class ProfileManager {

    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private FirebaseAuth mAuth;

    public ProfileManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Saves the desired user into firebase if they do no exist already. If they
     * already exist, updates the information.
     * Note: the userID is the key for the document in firebase.
     *
     * @param user the user profile that you want to save
     */
    public void saveUser(UserProfile user) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String uid = firebaseUser.getUid();

        user.setUserID(uid);

        DocumentReference docRef = usersRef.document(user.getProfileID());
        docRef.set(user);
    }

    public interface UserProfileCallBack {
        void onComplete(UserProfile user);
    }

    /**
     * Get the user profile of the current signed in user
     */
    public void getUserProfile(UserProfileCallBack callback) {
        // get current user
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        // retrieve the user in firebase
        DocumentReference docRef = usersRef.document(uid);

        usersRef.document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()){
                UserProfile userProfile = document.toObject(UserProfile.class);
                callback.onComplete(userProfile);
            }
        });

    }
}
