package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.WaitlistEntry;

import java.util.List;

/**
 * This class is used to represent the waitlist for an event.
 *Contains logic for updating waitlist to the firestore db.
 */
public class WaitlistFirebase {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference waitlistRef = db.collection("waitlist");

    /**
     * This method is used to update/add to the waitlist for an event in the firestore db.
     * @param eventId The id of the event to update the waitlist for.
     * @param entry The entry to add to the waitlist.
     * @return A boolean indicating whether the update was successful.
    **/
    public boolean updateWaitlist(String eventId, WaitlistEntry entry) {
        ...
    }

    /**
     * This method is used to remove an entry from the waitlist for an event in the firestore db.
     * @param eventId The id of the event to remove the entry from.
     * @param entry The entry to remove from the waitlist.
     * @return A boolean indicating whether the removal was successful.
    **/
    public boolean removeWaitlistEntry(String eventId, WaitlistEntry entry) {
        ...
    }
    /**
     * This method is used to get the waitlist for an event from the firestore db.
     * @param eventId The id of the event to get the waitlist for.
     * @return A list of WaitlistEntry objects representing the waitlist for the event.
    **/
    public List<WaitlistEntry> getWaitlist(String eventId) {
        ...
    }
    /**
     * This method is used to get the number of entries in the waitlist for an event in the firestore db.
     * @param eventId The id of the event to get the waitlist count for.
     * @return An integer representing the number of entries in the waitlist for the event.
     **/
    public int getWaitlistCount(String eventId) {
        ...
    }

    /**
     * This method is to verify whether an entrant is in the waitlist already/
     * @param eventId The id of the event to check the waitlist for.
     * @param entrantId The id of the entrant to check the waitlist for.
     * @return A boolean indicating whether the entrant is in the waitlist.
    **/
    public boolean isEntrantInWaitlist(String eventId, String entrantId) {
        ...
    }
}
