package com.example.eventlotterysystem.model;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.NotificationSystem;

import java.util.List;

/** TODO: JavaDoc to be made.
 */
public class Notification extends NotificationSystem {

    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    //Lottery Winner or Loser. Maybe add event name in the future?
    public void notifyWinner(String entrantId, String eventId) {
        sendNotification(entrantId, "You have won the lottery for an event! Please click to see more.", eventId, "System");
    }

    public void notifyLoser(String entrantId, String eventId) {
        sendNotification(entrantId, "You have lost the lottery for an event! Please click to see more.", eventId, "System");
    }

    public void notifyPrivateInvite(String entrantId, String eventId) {
        sendNotification(entrantId, "You have been invited to a private event", eventId, "System");
    }

    public void notifyOrganizerInvite(String entrantId, String eventId) {
        sendNotification(entrantId, "You have been invited to become a co-organizer of this event.", eventId, "System");
    }

    /**
     * Prebased NotificationStatus for the Organizers.
     * Also logs it for the System.
     * TODO: Maybe add organizer name who triggered this state?
     * @param message
     * @param eventId
     * @param status
     */
    private void notifyAllWithStatus(String message, String eventId, int status) {
        entrantListFirebase.getEntrantsByStatus(eventId, status)
                .addOnSuccessListener(entrantList -> {
                    for (EntrantListEntry entrant : entrantList) {
                        String entrantId = entrant.getEntrantId();
                        sendNotification(entrantId, message, eventId, "Organizer");
                        logNotification(entrantId, eventId, message);
                    }
                });
    }
    // All the premade status. Organizers will call this instead.
    public void notifyAllWaiting(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 1);
    }
    public void notifyAllInvited(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 2);
    }
    public void notifyAllAccepted(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 3);
    }
    public void notifyAllCancelled(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 4);
    }


    public void notifyAllEntrants(String message, String eventId) {
        entrantListFirebase.getEntrantList(eventId)
                .addOnSuccessListener(Entrantlist -> {
                    for (EntrantListEntry entrant : Entrantlist) {
                        sendNotification(entrant.getEntrantId(), message, eventId, "");
                    }
                });
    }

    // Ill probably borrow admin code for the delete user?
    public void notifySelectEntrants(String message, String eventId, List<EntrantListEntry> selectedList) {
        for (EntrantListEntry entrant : selectedList) {
            sendNotification(entrant.getEntrantId(), message, eventId, "");
        }
    }
}

