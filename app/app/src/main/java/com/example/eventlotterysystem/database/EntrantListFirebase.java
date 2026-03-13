package com.example.eventlotterysystem.database;

import androidx.annotation.NonNull;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
/**
 * This class is used to represent the waitlist for an event.
 *Contains logic for updating waitlist to the firestore db.
 * @author Jaylin
 */
public class EntrantListFirebase {
    // See https://firebase.google.com/docs/firestore/quickstart#java
    // and https://firebase.google.com/docs/reference/android/com/google/firebase/ml/common/modeldownload/FirebaseModelManager
    // For documentation referenced for this implementation
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * This method is used to get the reference to the entrantlistRef for an event in the firestore db.
     * @param eventId
     *  The id of the event to get the entrantlistRef for.
     * @return A reference to the entrantlistRef for the event.
     */
    // lab 5, and https://firebase.google.com/docs/reference/js/v8/firebase.firestore.CollectionReference
    private CollectionReference entrantlistRef(@NonNull String eventId) {
        return db.collection("events").document(eventId).collection("EntrantList");
    }// in events, go to specific event, then to entrantlistRef

    /**
     * This method is used to update/add to the entrant list for an event in the firestore db.
     * if exist update, if new insert
     * @param eventId The id of the event to update the list for.
     * @param entry The entry to add to the list.
    **/
    public Task<Void> upsertEntry(@NonNull String eventId, @NonNull EntrantListEntry entry) {
        return entrantlistRef(eventId)
                .document(entry.getEntrantId())
                .set(entry);
    }

    /**
     * This method is used to remove an entry from the list for an event in the firestore db.
     * @param eventId The id of the event to remove the entry from.
     * @param entrantId The entry to remove from the list.
     **/
    public Task<Void> removeEntrantListEntry(@NonNull String eventId, @NonNull String entrantId) {
        // see lab 5 logic for reference
        return entrantlistRef(eventId).document(entrantId).delete();
    }

    /**
     * This method is used to check if an entrant is in the entrantlistRef for an event in the firestore db.
     * @param eventId
     * @param entrantId
     * @return
     */
    public Task<EntrantListEntry> getEntry(@NonNull String eventId, @NonNull String entrantId) {
        return entrantlistRef(eventId)
                .document(entrantId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        return null;
                    }
                    return task.getResult().toObject(EntrantListEntry.class);
                });
    }

    /**
     * This method is used to check an entrants stored status in the db.
     * @param eventId
     * @param entrantId
     * @return An integer representing the status of the entrant.
     */
    public Task<Integer> getEntrantStatus(@NonNull String eventId, @NonNull String entrantId) {
        return entrantlistRef(eventId)
                .document(entrantId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        return -1; // no entry exists
                    }

                    EntrantListEntry entry = task.getResult().toObject(EntrantListEntry.class);
                    if (entry == null) return -1;

                    return entry.getStatus();
                });
    }

    /**
     * This method is used to change the set status of the entrant in the list for an event in the firestore db.
     * @param eventId
     * @param entrantId
     * @param status
     * @return void
     */
    public Task<Void> updateStatus(@NonNull String eventId,
                                   @NonNull String entrantId,
                                   int status) {
        return entrantlistRef(eventId)
                .document(entrantId)
                .update("status", status);
    }

    /**
     * This method is used to get the number of entries in the entrantlistRef for an event in the firestore db.
     * @param eventId The id of the event to get the entrantlistRef count for.
     * @return An integer representing the number of entries in the entrantlistRef for the event.
     **/
    public Task<Integer> getWaitlistCount(@NonNull String eventId) {
        return entrantlistRef(eventId).whereEqualTo("status", EntrantListEntry.STATUS_WAITLIST).get().continueWith(task -> {
            if (!task.isSuccessful()|| task.getResult() == null )  return 0;
            return task.getResult().size();
        });
    }

    /**
     * This method is used to get the list of entrants in the entrantlistRef for an event in the firestore db.
     * @param eventId
     *  The id of the event to get the entrants for.
     * @return
     *  A list of EntrantListEntry objects representing the entrants in the entrantlistRef for the event.
     */
    public Task<List<EntrantListEntry>> getEntrantList(@NonNull String eventId) {
        return entrantlistRef(eventId)
                .get()
                .continueWith(task -> {
                    List<EntrantListEntry> entrants = new ArrayList<>(); // make list
                    if(!task.isSuccessful() || task.getResult() == null) {
                        return entrants;
                    }
                    for (DocumentSnapshot doc:task.getResult().getDocuments()){
                        EntrantListEntry entry = doc.toObject(EntrantListEntry.class);
                        if (entry != null) {
                            entrants.add(entry); // append entrant to list
                        }
                    }
                    return entrants;
                });
    }

    /**
     * This method is used to get the list of entrants in the entrantlistRef for an event in the firestore db.
     * @param eventId
     *  The id of the event to get the entrants for.
     * @param status
     *  The status of the entrants to get.
     * @return
     *  A list of EntrantListEntry objects representing the entrants in the entrantlistRef for the event.
     */
    public Task<List<EntrantListEntry>> getEntrantsByStatus(@NonNull String eventId, int status) {
        return entrantlistRef(eventId)
                .whereEqualTo("status", status) //only for the type of list wanted
                .get()
                .continueWith(task -> {
                    List<EntrantListEntry> entrants = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return entrants;
                    }
                    for (DocumentSnapshot doc:task.getResult().getDocuments()) {
                        EntrantListEntry entry = doc.toObject(EntrantListEntry.class);
                        if (entry != null) {
                            entrants.add(entry);
                        }
                    }
                    return entrants;
                });
    }
}
