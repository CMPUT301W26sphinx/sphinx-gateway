package com.example.eventlotterysystem.model;

import android.util.Log;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.NotificationSystem;
import com.example.eventlotterysystem.database.ProfileManager;

import java.util.List;

/** Notification class, premade notification that is called instead of using notificationsystem.
 * TODO: loadOrganizerName(); is always repeating.
 */
public class Notification extends NotificationSystem {
    private String organizerName;
    private String eventName;
    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    private void loadOrganizerName() {
        ProfileManager manager = ProfileManager.getInstance();
        manager.getUserProfile(user -> {
            if (user.getFirstName() != null && user.getLastName() != null) {
                organizerName = user.getFirstName() + " " + user.getLastName();
            }
        });
    }

    //Lottery Winner or Loser.
    //Really cursed setup, maybe compact it somehow.
    public void notifyWinner(String entrantId, String eventId) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                String eventName = event.getTitle();
                sendNotification(entrantId, "You have won the lottery for the event " + eventName + "! Please click to see more.", eventId, "System");
            }
            @Override
            public void onError(Exception e) {
                Log.e("Notification", "Failed to fetch event for notifyWinner: " + eventId, e);
            }
        });
    }
    public void notifyLoser(String entrantId, String eventId) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                String eventName = event.getTitle();
                sendNotification(entrantId, "You have lost the lottery for the event " + eventName + "! Please click to see more.", eventId, "System");
            }
            @Override
            public void onError(Exception e) {
                Log.e("Notification", "Failed to fetch event for notifyLoser: " + eventId, e);
            }
        });
    }

    public void notifyPrivateInvite(String entrantId, String eventId) {
        loadOrganizerName();
        sendNotification(entrantId, "You have been invited to a private event by "+ organizerName, eventId, "System");
    }

    public void notifyOrganizerInvite(String entrantId, String eventId) {
        loadOrganizerName();
        sendNotification(entrantId, "You have been invited to become a co-organizer of an event by "+ organizerName, eventId, "System");
    }

    /**
     * Prebased NotificationStatus for the Organizers.
     * Also logs it for the System.
     * * @param message
     * @param eventId
     * @param status
     */
    private void notifyAllWithStatus(String message, String eventId, int status) {
        loadOrganizerName();
        entrantListFirebase.getEntrantsByStatus(eventId, status)
                .addOnSuccessListener(entrantList -> {
                    for (EntrantListEntry entrant : entrantList) {
                        String entrantId = entrant.getEntrantId();
                        sendNotification(entrantId, message, eventId, "Organizer " + organizerName);
                        logNotification(eventId, message);
                    }
                });
    }
    // All the premade status. Organizers will call this instead.
    public void notifyAllWaiting(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 1);
    }
    public void notifyAllSelected(String message, String eventId) {
        notifyAllWithStatus(message, eventId, 2);
    }
    public void notifyAllEnrolled(String message, String eventId) {
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

