package com.example.eventlotterysystem.model;

import com.example.eventlotterysystem.database.ProfileManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationSystem {

    private final ProfileManager profileManager;

    public NotificationSystem() {
        this.profileManager = new ProfileManager();
    }

    /**
     * Sends a notification to a user if their notification preference is enabled.
     * Appends to the user's Notifications list in Firestore.
     *
     * @param entrantId The ID of the user to notify.
     * @param message   The notification message to append.
     * @author Bryan Jonathan
     */
    public void sendNotification(String entrantId, String message) {
        profileManager.getUserProfileById(entrantId, user -> {
            if (user == null) return;
            if (!user.getNotificationPreference()) return;

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(entrantId)
                    .update("notification", FieldValue.arrayUnion(message));
        });
    }
}
