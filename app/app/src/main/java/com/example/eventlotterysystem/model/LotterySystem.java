package com.example.eventlotterysystem.model;

import android.util.Log;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;

import java.util.Random;
/** Basic Lottery System
 * TL;DR, When lottery is triggered, it passes the amount.
 * Auto notifies the user who wins with predetermined notification.
 * @author Bryan Jonathan
 */
public class LotterySystem {
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
    private final EventRepository eventRepository = new EventRepository();
    private final Notification notification = new Notification();
    protected void notifyWinner(String entrantId, String eventId) {
        notification.notifyWinner(entrantId, eventId);
    }

    protected void notifyLoser(String entrantId, String eventId) {
        notification.notifyLoser(entrantId, eventId);
    }

    /** Starts the lottery system, looks at eventid, then chooses based on lottery.
     * Checksum is built in. It is remainingSpots = capacity - invited - registered
     * It then goes and chooses if the remainingspots or the entrantAmount is the minimum.
     * This ensures that it will never surpass the limit.
     * @param eventId eventid is passed by from triggering lottery
     * @param entrantAmount the amount of entrant that is wanted on
     */
    private void start(String eventId, int entrantAmount, Runnable onComplete) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                int capacity = event.getCapacity();

                //CHECKSUM FOR THE LOTTERY, SEE IF IT HAS PASSED OR not
                entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_INVITED)
                        .addOnSuccessListener(invitedEntrants ->
                entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_REGISTERED)
                        .addOnSuccessListener(registeredEntrants -> {
                            if (capacity <= 0) {
                                runLottery(eventId, entrantAmount, entrantAmount, onComplete);
                                return;
                            }
                            int remainingSpots = capacity - invitedEntrants.size() - registeredEntrants.size();
                            if (remainingSpots <= 0) return;

                            runLottery(eventId, entrantAmount, remainingSpots, onComplete);
                        })
                    );
            }
            @Override
            public void onError(Exception e) {
                Log.e("LotterySystem", "Failed to fetch event capacity for eventId: " + eventId, e);
            }
        });
    }
    private void runLottery(String eventId, int entrantAmount, int remainingSpots, Runnable onComplete) {
        entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_WAITLIST)
                .addOnSuccessListener(waitlistEntrants -> {
                    Random rand = new Random();
                    int inviteCount = Math.min(Math.min(remainingSpots, entrantAmount), waitlistEntrants.size());
                    Log.d("LotterySystem", "inviteCount=" + inviteCount + " waitlist=" + waitlistEntrants.size());

                    for (int i = 0; i < inviteCount; i++) {
                        int randomIndex = rand.nextInt(waitlistEntrants.size());
                        EntrantListEntry chosen = waitlistEntrants.remove(randomIndex);

                        // Notify winner only AFTER status is successfully updated FIX!
                        entrantListFirebase.updateStatus(eventId, chosen.getEntrantId(), EntrantListEntry.STATUS_INVITED)
                                .addOnSuccessListener(unused -> {
                                    Log.d("LotterySystem", "Invited: " + chosen.getEntrantId());
                                    notifyWinner(chosen.getEntrantId(), eventId);
                                })
                                .addOnFailureListener(e -> Log.e("LotterySystem", "Failed to invite", e));
                    }

                    // Run the callback (e.g. notify losers) after lottery selection is done
                    if (onComplete != null) onComplete.run();
                });
    }

    /** Starts the lottery system, for a singular person. Useful for when someone invited cancels
     * @param eventId eventid is passed by from triggering lottery
     */
    public void singularLottery(String eventId){
        start(eventId,1, null);
    }

    /** Starts the lottery system for a specified sample amount chosen by organizer
     * @param eventId eventid is passed by from triggering lottery
     * @param sampleAmount Sample amount that organizer chooses
     */
    public void sampleLottery(String eventId, int sampleAmount){
        start(eventId,sampleAmount, null);
    }

    /** Starts the lottery system up to capacity, or as I like to call it, first lottery!
     * @param eventId eventid is passed by from triggering lottery
     */
    public void firstLottery(String eventId){
        //this is really awful, but entrant amount is just endcap. This will basically max it to capacity.
        start(eventId, 9999999, () -> {
            // Notify losers only AFTER the lottery winners have been selected
            entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_WAITLIST)
                    .addOnSuccessListener(waitlistEntrants -> {
                        for (EntrantListEntry entrant : waitlistEntrants) {
                            notifyLoser(entrant.getEntrantId(), eventId);
                        }
                    });
        });
    }
    /** Redraw lottery. Same as firstlottery, but without notification that entrant lost.
     * @param eventId eventid is passed by from triggering lottery
     */
    public void redrawLottery(String eventId){
        start(eventId, 99999999, null);
    }
}