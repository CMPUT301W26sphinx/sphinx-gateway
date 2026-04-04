package com.example.eventlotterysystem.database;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationSystem {
    private final ProfileManager profileManager;
    public NotificationSystem() {
        this.profileManager = ProfileManager.getInstance();
    }

    /**
     * Sends a STRUCTURED notification to a user if their notification preference is enabled.
     * "message|eventId|sender"
     * Appends to the user's Notifications list in Firestore.
     *
     * @param entrantId The ID of the user to notify.
     * @param sender  Who sent the message - for now it will be organizer from lottery.
     * @param eventId The notification message to append.
     * @author Bryan Jonathan
     */
    public void sendNotification(String entrantId, String message, String eventId, String sender) {
        profileManager.getUserProfileById(entrantId, user -> {
            if (user == null) return;
            if (!user.getNotificationPreference()) return;

            String structMessage = message + "|" + eventId + "|" + sender;

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(entrantId)
                    .update("notification", FieldValue.arrayUnion(structMessage));
        });
    }
    //Special use case, basically has it make sure that it opens dialog when there's that Ask option.
    //Used for private and notify selected entrants.
    public void sendNotificationAsk(String entrantId, String message, String eventId, String sender){
        profileManager.getUserProfileById(entrantId, user -> {
            if (user == null) return;
            if (!user.getNotificationPreference()) return;

            String structMessage = message + "|" + eventId + "|" + sender + "|ask";

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(entrantId)
                    .update("notification", FieldValue.arrayUnion(structMessage));
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
    public void deleteNotification(String userId, String message) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("notification", FieldValue.arrayRemove(message));
    }
    public void logNotification(String eventId, String message) {
        java.util.Map<String, Object> logData = new java.util.HashMap<>();
        String organizerId = ProfileManager.getInstance().getUserID();
        // OrganizerID, basically You.
        logData.put("OrganizerID", organizerId);
        logData.put("EventID", eventId);
        logData.put("Message", message);
        logData.put("Time", com.google.firebase.Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("logs_notification")
                .add(logData);
    }
}