package com.example.eventlotterysystem.model;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.NotificationSystem;

import java.util.Random;
/** Basic Lottery System
 * TL;DR, When lottery is triggered, it passes capacity.
 * When someone cancels, it should triggers this again, but only 1 person.
 * Auto notifies the user who wins with predetermined notification.
 * @author Bryan Jonathan
 */
public class LotterySystem {
    private final NotificationSystem notificationSystem = new NotificationSystem();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
    private final EventRepository eventRepository = new EventRepository();

    /** Starts the lottery system, looks at eventid, then chooses based on lottery.
     * @param eventId eventid is passed by from triggering lottery
     * @param capacity capacity is passed by.
     */
    public void start(String eventId, int capacity) {
        entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_INVITED)
                .addOnSuccessListener(invitedEntrants -> {
            entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_REGISTERED)
                    .addOnSuccessListener(registeredEntrants -> {

                //check if invited + registered is over capacity
                int remainingSpots = capacity - invitedEntrants.size() - registeredEntrants.size();
                if (remainingSpots <= 0) return;

                entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_WAITLIST)
                        .addOnSuccessListener(waitlistEntrants -> {

                    //LOTTERY LOGIC
                    Random rand = new Random();
                    int inviteCount = Math.min(remainingSpots, waitlistEntrants.size());

                    for (int i = 0; i < inviteCount; i++) {
                        int randomIndex = rand.nextInt(waitlistEntrants.size());
                        EntrantListEntry chosen = waitlistEntrants.remove(randomIndex);

                        entrantListFirebase.updateStatus(
                                eventId,
                                chosen.getEntrantId(),
                                EntrantListEntry.STATUS_INVITED
                        );
                        notifyEntrantLottery(chosen.getEntrantId(), eventId);
                    }
                });
            });
        });
    }

    /** Lottery System specific notification system.
     * @param entrantId used to see which user to notify
     * @param eventId eventid is passed by from triggering lottery
     */
    private void notifyEntrantLottery(String entrantId, String eventId) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                notificationSystem.sendNotification(
                        entrantId,
                        "You have been selected for '" + event.getTitle() + "'! Please confirm your registration."
                );
            }
            @Override
            public void onError(Exception e) {
                notificationSystem.sendNotification(
                        entrantId,
                        "You have been selected for an event! Please confirm your registration."
                );
            }
        });
    }
}