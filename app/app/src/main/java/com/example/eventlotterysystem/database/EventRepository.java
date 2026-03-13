package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Callback for list of events
    public interface EventCallback {
        void onEventsLoaded(List<Event> events);
        void onError(Exception e);
    }

    // Callback for a single event
    public interface SingleEventCallback {
        void onEventLoaded(Event event);
        void onError(Exception e);
    }

    // Callback for delete operation
    public interface OnDeleteListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Fetch all events from Firestore.
     */
    public void getEvents(EventCallback callback) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId());
                        events.add(event);
                    }
                    callback.onEventsLoaded(events);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetch a single event by its document ID.
     */
    public void getEvent(String eventId, SingleEventCallback callback) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        event.setEventId(documentSnapshot.getId());
                        callback.onEventLoaded(event);
                    } else {
                        callback.onError(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Delete an event by its document ID.
     */
    public void removeEvent(String eventId, OnDeleteListener listener) {
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}