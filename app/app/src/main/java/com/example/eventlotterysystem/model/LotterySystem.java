package com.example.eventlotterysystem.model;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/** Basic Lottery System
 * TODO: add lottery system testfiles.
 * TODO: add checksum if it surpasses the limit. However, this might not be required due to how we're implementing lottery
 * TL;DR, When lottery is triggered, it passes capacity. When someone cancels, it triggers this again, but only 1 person.
 * @author Bryan Jonathan
 */
public class LotterySystem {
    /** Starts the lottery system, looks at eventid, then chooses based on lottery.
     * @param eventId eventid is passed by from triggering lottery
     * @param capacity capacity is passed by.
     */
    public void start(String eventId, int capacity) {
        CollectionReference entrantListRef = FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .collection("EntrantList");

        entrantListRef.get().addOnSuccessListener(querySnapshot -> {
            List<EntrantListEntry> waitlistEntrants = new ArrayList<>();

            // Checks
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                EntrantListEntry entry = doc.toObject(EntrantListEntry.class);
                if (entry != null && entry.getStatus() == EntrantListEntry.STATUS_WAITLIST) {
                    waitlistEntrants.add(entry);
                }
            }

            // 2. Randomly pick up to 'capacity' entrants
            Random rand = new Random();
            int inviteCount = Math.min(capacity, waitlistEntrants.size());

            for (int i = 0; i < inviteCount; i++) {
                int randomIndex = rand.nextInt(waitlistEntrants.size());
                EntrantListEntry chosen = waitlistEntrants.get(randomIndex);

                // 3. Update status locally and in Firestore
                chosen.setStatus(EntrantListEntry.STATUS_INVITED);
                entrantListRef.document(chosen.getEntrantId())
                        .set(chosen)
                        .addOnSuccessListener(unused -> notifyEntrant(chosen.getEntrantId()))
                        .addOnFailureListener(e -> e.printStackTrace());

                waitlistEntrants.remove(randomIndex);
            }

        }).addOnFailureListener(e -> e.printStackTrace());
    }

    private void notifyEntrant(String entrantId) {
        // TODO: Trigger Notification model
        System.out.println("Notified: " + entrantId);
    }
}