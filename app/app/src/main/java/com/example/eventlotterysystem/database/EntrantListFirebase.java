package com.example.eventlotterysystem.database;

import androidx.annotation.NonNull;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class is used to represent the waitlist for an event.
 *Contains logic for updating waitlist to the firestore db.
 */
public class EntrantListFirebase {
    // See https://firebase.google.com/docs/firestore/quickstart#java
    // and https://firebase.google.com/docs/reference/android/com/google/firebase/ml/common/modeldownload/FirebaseModelManager
    // For documentation referenced for this implementation
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * This method is used to get the reference to the waitlist for an event in the firestore db.
     * @param eventId
     *  The id of the event to get the waitlist for.
     * @return A reference to the waitlist for the event.
     */
    // lab 5, and https://firebase.google.com/docs/reference/js/v8/firebase.firestore.CollectionReference
    private CollectionReference waitlistRef(@NonNull String eventId) {
        return db.collection("Events").document(eventId).collection("EntrantList");
    }// in events, go to specific event, then to waitlist

    /**
     * This method is used to update/add to the waitlist for an event in the firestore db.
     * @param eventId The id of the event to update the waitlist for.
     * @param entry The entry to add to the waitlist.
    **/
    public Task<Void> updateWaitlist(@NonNull String eventId, @NonNull EntrantListEntry entry) {
        return waitlistRef(eventId).document(entry.getEntrantId()).set(entry);
    }

    /**
     * This method is used to remove an entry from the waitlist for an event in the firestore db.
     * @param eventId The id of the event to remove the entry from.
     * @param entrantId The entry to remove from the waitlist.
     **/
    public Task<Void> removeWaitlistEntry(@NonNull String eventId, @NonNull String entrantId) {
        // see lab 5 logic for reference
        return waitlistRef(eventId).document(entrantId).delete();
    }
    /**
     * This method is to verify whether an entrant is in the waitlist already/
     * @param eventId The id of the event to check the waitlist for.
     * @param entrantId The id of the entrant to check the waitlist for.
     * @return A boolean indicating whether the entrant is in the waitlist.
     **/
    public Task<Boolean> isEntrantInWaitlist(@NonNull String eventId, @NonNull String entrantId) {
        return waitlistRef(eventId).document(entrantId).get().continueWith(task -> {
            if (!task.isSuccessful()) return false;
            DocumentSnapshot document = task.getResult();
            return document.exists();
        });
    }
    /**
     * This method is used to get the number of entries in the waitlist for an event in the firestore db.
     * @param eventId The id of the event to get the waitlist count for.
     * @return An integer representing the number of entries in the waitlist for the event.
     **/
    public Task<Integer> getWaitlistCount(@NonNull String eventId) {
        return waitlistRef(eventId).get().continueWith(task -> {
            if (!task.isSuccessful())return 0;
            return task.getResult().size();
        });
    }
}
