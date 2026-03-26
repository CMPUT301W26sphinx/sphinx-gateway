package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.Log;
import com.example.eventlotterysystem.model.LogItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository class for retrieving and formatting log data from Firestore.
 */
public class LogRepository {
    private static final String TAG = "LogRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback interface used to return logs or an error.
     */
    public interface LogsCallback {
        /**
         * Called when logs are successfully loaded.
         *
         * @param logs the list of loaded log items
         */
        void onLogsLoaded(List<LogItem> logs);

        /**
         * Called when an error occurs while loading logs.
         *
         * @param e the exception that occurred
         */
        void onError(Exception e);
    }

    /**
     * Retrieves all logs from Firestore, ordered by time in descending order.
     *
     * @param callback callback used to return the loaded logs or an error
     */
    public void getAllLogs(LogsCallback callback) {
        db.collection("logs_notification")
                .orderBy("Time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Log> rawLogs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Log log = doc.toObject(Log.class);
                        rawLogs.add(log);
                    }
                    fetchNames(rawLogs, callback);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches event names and user names for each raw log.
     *
     * @param rawLogs the list of raw logs from Firestore
     * @param callback callback used to return the final log items
     */
    private void fetchNames(List<Log> rawLogs, LogsCallback callback) {
        List<LogItem> result = new ArrayList<>();
        if (rawLogs.isEmpty()) {
            callback.onLogsLoaded(result);
            return;
        }

        AtomicInteger remaining = new AtomicInteger(rawLogs.size());
        for (Log log : rawLogs) {
            String eventId = log.getEventID();
            String entrantId = log.getEntrantID();

            // If event ID is missing, handle immediately
            if (eventId == null || eventId.isEmpty()) {
                handleMissingEvent(log, entrantId, result, remaining, callback);
                continue;
            }

            // Fetch event name
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(eventDoc -> {
                        String eventName = eventDoc.getString("title");
                        if (eventName == null) eventName = "Unknown Event";
                        final String finalEventName = eventName;

                        // Now handle user name
                        handleUserFetch(log, entrantId, finalEventName, result, remaining, callback);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e(TAG, "Error fetching event", e);
                        // Still try to add log with unknown event
                        handleUserFetch(log, entrantId, "Unknown Event", result, remaining, callback);
                    });
        }
    }

    /**
     * Fetches the user's name using the entrant ID and then adds the log item.
     *
     * @param log the original log object
     * @param entrantId the ID of the entrant
     * @param eventName the resolved event name
     * @param result the list where final log items are stored
     * @param remaining counter used to track unfinished async tasks
     * @param callback callback used to return the final results
     */
    private void handleUserFetch(Log log, String entrantId, String eventName,
                                 List<LogItem> result, AtomicInteger remaining, LogsCallback callback) {
        if (entrantId == null || entrantId.isEmpty()) {
            // No user ID – add log with unknown user
            addLogItem(log, eventName, "Unknown User", result, remaining, callback);
            return;
        }

        db.collection("users").document(entrantId).get()
                .addOnSuccessListener(userDoc -> {
                    String firstName = userDoc.getString("firstName");
                    String lastName = userDoc.getString("lastName");
                    String userName = firstName;
                    if (lastName != null) userName += " " + lastName;
                    if (userName == null || userName.trim().isEmpty()) {
                        userName = "Unknown User";
                    }
                    addLogItem(log, eventName, userName, result, remaining, callback);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error fetching user", e);
                    addLogItem(log, eventName, "Unknown User", result, remaining, callback);
                });
    }

    /**
     * Handles the case where the event ID is missing.
     *
     * @param log the original log object
     * @param entrantId the ID of the entrant
     * @param result the list where final log items are stored
     * @param remaining counter used to track unfinished async tasks
     * @param callback callback used to return the final results
     */
    private void handleMissingEvent(Log log, String entrantId, List<LogItem> result,
                                    AtomicInteger remaining, LogsCallback callback) {
        // Event ID missing – try to get user name anyway, but event name is "Unknown Event"
        handleUserFetch(log, entrantId, "Unknown Event", result, remaining, callback);
    }

    /**
     * Creates a LogItem and adds it to the result list.
     * When all log items have been processed, the callback is triggered.
     *
     * @param log the original log object
     * @param eventName the resolved event name
     * @param userName the resolved user name
     * @param result the list where final log items are stored
     * @param remaining counter used to track unfinished async tasks
     * @param callback callback used to return the final results
     */
    private void addLogItem(Log log, String eventName, String userName,
                            List<LogItem> result, AtomicInteger remaining, LogsCallback callback) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timeStr = log.getTime() != null ? sdf.format(log.getTime().toDate()) : "";
        result.add(new LogItem(eventName, userName, log.getMessage(), timeStr));

        if (remaining.decrementAndGet() == 0) {
            callback.onLogsLoaded(result);
        }
    }
}