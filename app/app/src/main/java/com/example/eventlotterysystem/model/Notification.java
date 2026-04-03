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
    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    //This fixes it being too fast, and sending null.
    //https://www.geeksforgeeks.org/java/asynchronous-synchronous-callbacks-java/
    private void loadOrganizerName(OrganizerNameCallback callback) {
        ProfileManager.getInstance().getUserProfile(user -> {
            String name = (user.getFirstName() != null)
                    ? user.getFirstName()
                    : "An organizer";
            callback.onLoaded(name);
        });
    }

    private interface OrganizerNameCallback {
        void onLoaded(String name);
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
        loadOrganizerName(name ->
                sendNotificationAsk(entrantId,
                        "You have been invited to a private event by " + name + ". Would you like to join the private waitlist?",
                        eventId, "System")
        );
    }
    public void notifyInvite(String entrantId, String eventId) {
        loadOrganizerName(name ->
                sendNotificationAsk(entrantId,
                        "You have been invited to a event by " + name + ". Would you like to join the private waitlist?",
                        eventId, "System")
        );
    }

    public void notifyOrganizerInvite(String entrantId, String eventId) {
        loadOrganizerName(name ->
                sendNotification(entrantId,
                        "You have been invited to become a co-organizer of this event by " + name + ".",
                        eventId, "System")
        );
    }

    /**
     * Prebased NotificationStatus for the Organizers.
     * Also logs it for the Database.
     * * @param message
     *
     * @param eventId
     * @param status
     */
    private void notifyAllWithStatus(String message, String eventId, int status) {
        loadOrganizerName(name ->
                entrantListFirebase.getEntrantsByStatus(eventId, status)
                        .addOnSuccessListener(entrantList -> {
                            for (EntrantListEntry entrant : entrantList) {
                                String entrantId = entrant.getEntrantId();
                                sendNotification(entrantId, message, eventId, "Organizer " + name);
                                logNotification(eventId, message);
                            }
                        })
        );
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
}

