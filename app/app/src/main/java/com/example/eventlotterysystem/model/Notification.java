package com.example.eventlotterysystem.model;

import android.util.Log;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.NotificationSystem;
import com.example.eventlotterysystem.database.ProfileManager;

import java.util.List;

/** Notification class, premade notification that is called instead of using notificationsystem directly.
 * More efficient than writing allllll the messages in allll the functions.
 * @author Bryan Jonathan
 */
public class Notification extends NotificationSystem {
    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    /** Used for if notificiation requires  organizer name (you)
     * Source used for fix, because it's too fast and sending null.
     * https://www.geeksforgeeks.org/java/asynchronous-synchronous-callbacks-java/
     * @param callback
     */
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

    /** Notifies winners, used by lotterysystem
     * @param entrantId
     * @param eventId
     * @author Bryan Jonathan
     */
    public void notifyWinner(String entrantId, String eventId) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                String eventName = event.getTitle();
                sendNotification(entrantId, "You have won the lottery for the event " + eventName + "! Please click to see more.", eventId, "System");
            }

            @Override
            public void onError(Exception e) {
                //this means that if it is not able to catch eventID, it will still pass on.
                sendNotification(entrantId,"You have won a lottery for an event!",eventId,"System");
                Log.e("Notification", "Failed to fetch event for notifyWinner: " + eventId, e);
            }
        });
    }
    /** Notifies losers, only triggered on first lottery.
     * @param entrantId
     * @param eventId
     * @author Bryan Jonathan
     */
    public void notifyLoser(String entrantId, String eventId) {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                String eventName = event.getTitle();
                sendNotification(entrantId, "You have lost the lottery for the event " + eventName + "! Please click to see more.", eventId, "System");
            }

            @Override
            public void onError(Exception e) {
                sendNotification(entrantId,"You have lost a lottery for an event.",eventId,"System");
                Log.e("Notification", "Failed to fetch event for notifyLoser: " + eventId, e);
            }
        });
    }

    /** Notification for private event. This also has it so that notification add the |ask| feature
     * where it can open notification and have it auto ask on the dialog box.
     * @param entrantId
     * @param eventId
     */
    public void notifyPrivateInvite(String entrantId, String eventId) {
        loadOrganizerName(name ->
                sendNotificationAsk(entrantId,
                        "You have been invited to a private event by " + name + ". Would you like to join the private waitlist?",
                        eventId, "System")
        );
    }
    /** Notification for event. This also has it so that notification add the |ask| feature
     * where it can open notification and have it auto ask on the dialog box.
     * Same same as private, but without the private.
     * @param entrantId
     * @param eventId
     */
    public void notifyInvite(String entrantId, String eventId) {
        loadOrganizerName(name ->
                sendNotificationAsk(entrantId,
                        "You have been invited to a event by " + name + ". Would you like to join the private waitlist?",
                        eventId, "System")
        );
    }

    /** Notifies userId that hey, he's now a coorganizer.
     * @param entrantId
     * @param eventId
     */
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

    /**
     * Unused for now. But, maybe?
     * @param message
     * @param eventId
     */
    public void notifyAllEntrants(String message, String eventId) {
        entrantListFirebase.getEntrantList(eventId)
                .addOnSuccessListener(Entrantlist -> {
                    for (EntrantListEntry entrant : Entrantlist) {
                        sendNotification(entrant.getEntrantId(), message, eventId, "");
                    }
                });
    }
}

