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

import java.util.ArrayList;
import java.util.List;

/**
 * Handles communication between local user profiles and firebase.
 *
 * @author Noah Zapisocki
 */
public class ProfileManager {

    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private FirebaseAuth mAuth;

    private ProfileManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        mAuth = FirebaseAuth.getInstance();
    }

    private static class Holder {
        private static final ProfileManager instance = new ProfileManager();
    }

    public static ProfileManager getInstance() {
        return Holder.instance;
    }

    /**
     * Get the user id of the signed in user
     *
     * @return
     */
    public String getUserID() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return user.getUid();
    }

    /**
     * Saves the desired user into firebase if they do no exist already. If they
     * already exist, updates the information.
     * Note: the userID is the key for the document in firebase.
     *
     * @param user the user profile that you want to save
     */
    public void saveUser(UserProfile user) {
        // get the UID
        String uid = getUserID();
        // set the ID for the profile
        user.setUserID(uid);
        // update in firebase
        usersRef.document(uid).set(user);
    }

    public interface UserProfileCallBack {
        void onComplete(UserProfile user);
    }

    /**
     * Get the user profile of the current signed in user
     *
     * @param callback
     */
    public void getUserProfile(UserProfileCallBack callback) {
        // get current user
        String uid = getUserID();
        // retrieve the user in firebase
        usersRef.document(uid).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                UserProfile userProfile = document.toObject(UserProfile.class);
                callback.onComplete(userProfile);
            }
        });
    }

    public interface AllUsersCallback {
        void onUsersLoaded(List<UserProfile> users);

        void onError(Exception e);
    }

    public void getAllUsers(AllUsersCallback callback) {
        usersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<UserProfile> users = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                UserProfile user = doc.toObject(UserProfile.class);
                user.setUserID(doc.getId()); // ensure ID is set
                users.add(user);
            }
            callback.onUsersLoaded(users);
        }).addOnFailureListener(callback::onError);
    }


    public interface OnDeleteListener {
        void onSuccess();

        void onError(Exception e);
    }

    public void deleteUser(String userId, OnDeleteListener listener) {
        usersRef.document(userId).delete().addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(listener::onError);
    }

    /**
     * Get the user profile of any user by their ID.
     * Used by systems like NotificationSystem that need to look up other users.
     *
     * @param userId   The ID of the user to fetch.
     * @param callback Returns the UserProfile, or null if not found.
     */
    public void getUserProfileById(String userId, UserProfileCallBack callback) {
        usersRef.document(userId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                UserProfile userProfile = document.toObject(UserProfile.class);
                callback.onComplete(userProfile);
            } else {
                callback.onComplete(null);
            }
        });
    }


}