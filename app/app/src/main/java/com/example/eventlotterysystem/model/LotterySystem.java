package com.example.eventlotterysystem.model;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.NotificationSystem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/** Basic Lottery System
 * TODO: polish the code up, change the message.
 * TODO: add checksum if it surpasses the limit. However, this might not be required due to how we're implementing lottery
 * TL;DR, When lottery is triggered, it passes capacity. When someone cancels, it triggers this again, but only 1 person.
 * @author Bryan Jonathan
 */
public class LotterySystem {
    private final NotificationSystem NotificationSystem = new NotificationSystem();
    private final EntrantListFirebase EntrantListFirebase = new EntrantListFirebase();

    /** Starts the lottery system, looks at eventid, then chooses based on lottery.
     * @param eventId eventid is passed by from triggering lottery
     * @param capacity capacity is passed by.
     */
    public void start(String eventId, int capacity) {
        EntrantListFirebase.getWaitlistedEntrants(eventId).addOnSuccessListener(waitlistEntrants -> {
            Random rand = new Random();
            int inviteCount = Math.min(capacity, waitlistEntrants.size());

            for (int i = 0; i < inviteCount; i++) {
                int randomIndex = rand.nextInt(waitlistEntrants.size());
                EntrantListEntry chosen = waitlistEntrants.remove(randomIndex);

                EntrantListFirebase.updateStatus(eventId, chosen.getEntrantId(), EntrantListEntry.STATUS_INVITED);
            }
        });
    }

    private void notifyEntrant(String entrantId) {
        NotificationSystem.sendNotification(entrantId, "You have been selected for an event! Please confirm your registration.");

    }
}