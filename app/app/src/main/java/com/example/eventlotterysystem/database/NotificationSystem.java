package com.example.eventlotterysystem.database;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    /** Called with the user's notifications (newest first), or an empty list on none/error. */
    public interface NotificationsCallback {
        void onResult(List<String> notifications);
    }
    /**
     * Fetches the "notification" array for the given user via ProfileManager.
     * Returns the list reversed so the newest message appears first.
     *
     * @param userId   The Firestore document ID of the user.
     * @param callback Receives the notification list (never null; empty on error/none).
     */
    public void getNotifications(String userId, NotificationsCallback callback) {
        profileManager.getUserProfileById(userId, user -> {
            if (user == null) {
                callback.onResult(Collections.emptyList());
                return;
            }

            List<String> raw = user.getNotification();
            if (raw == null || raw.isEmpty()) {
                callback.onResult(Collections.emptyList());
                return;
            }

            List<String> reversed = new ArrayList<>(raw);
            Collections.reverse(reversed);
            callback.onResult(reversed);
        });
    }
}