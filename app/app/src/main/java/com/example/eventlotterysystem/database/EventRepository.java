package com.example.eventlotterysystem.database;

import android.util.Log;

import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for retrieving and modifying event data.
 */
public class EventRepository {

    private static final String TAG = "EventRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface EventCallback {
        void onEventsLoaded(List<Event> events);
        void onError(Exception e);
    }

    public interface SingleEventCallback {
        void onEventLoaded(Event event);
        void onError(Exception e);
    }

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
                        Event event = documentToEvent(document);
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
                        Event event = documentToEvent(documentSnapshot);
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

    /**
     * Converts a Firestore document to an Event object manually.
     * This avoids type conversion issues with Timestamp.
     */
    private Event documentToEvent(com.google.firebase.firestore.DocumentSnapshot doc) {
        String id = doc.getId();
        String title = doc.getString("title");
        String description = doc.getString("description");
        String place = doc.getString("place");
        Long date = doc.getLong("date");
        Long capacity = doc.getLong("capacity");
        Long registrationStartDate = doc.getLong("registrationStartDate");
        Long registrationEndDate = doc.getLong("registrationEndDate");
        Long waitingListCount = doc.getLong("waitingListCount");
        String category = doc.getString("category");   // new field
        String privacy = doc.getString("privacy");
        String organizerId = doc.getString("organizerId");
        List<String> co_organizerIds = (List<String>) doc.get("coOrganizerIds");
        String imageData = doc.getString("imageData");   // <-- ADD THIS
        Boolean geoRequirementEnabled = doc.getBoolean("geoRequirementEnabled");
        String geoRequirementMode = doc.getString("geoRequirementMode");
        Double geoRequirementLatitude = doc.getDouble("geoRequirementLatitude");
        Double geoRequirementLongitude = doc.getDouble("geoRequirementLongitude");
        Double geoRequirementRadiusKm = doc.getDouble("geoRequirementRadiusKm");

        Event event = new Event(id, title, description);
        event.setPlace(place);
        event.setOrganizerId(organizerId);
        event.setPrivacy(privacy);
        if (date != null) event.setDate(date);
        if (capacity != null) event.setCapacity(capacity.intValue());
        if (registrationStartDate != null) event.setRegistrationStartDate(registrationStartDate);
        if (registrationEndDate != null) event.setRegistrationEndDate(registrationEndDate);
        if (waitingListCount != null) event.setWaitingListCount(waitingListCount.intValue());
        if (category != null) event.setCategory(category);
        if (co_organizerIds != null) event.setCoOrganizerIds(co_organizerIds);
        if (imageData != null) event.setImageData(imageData);   // <-- AND THIS
        if (geoRequirementEnabled != null) event.setGeoRequirementEnabled(geoRequirementEnabled);
        if (geoRequirementMode != null) event.setGeoRequirementMode(geoRequirementMode);
        if (geoRequirementLatitude != null) event.setGeoRequirementLatitude(geoRequirementLatitude);
        if (geoRequirementLongitude != null) event.setGeoRequirementLongitude(geoRequirementLongitude);
        if (geoRequirementRadiusKm != null) event.setGeoRequirementRadiusKm(geoRequirementRadiusKm);

        return event;
    }

    private Event documentToEvent(QueryDocumentSnapshot doc) {
        return documentToEvent((com.google.firebase.firestore.DocumentSnapshot) doc);
    }

    /**
     * Update the geolocation requirement settings for an event.
     * US-02.02.03
     */
    public void updateGeoRequirement(String eventId, boolean enabled, String mode,
                                     Double latitude, Double longitude, Double radiusKm,
                                     OnDeleteListener listener) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("geoRequirementEnabled", enabled);
        updates.put("geoRequirementMode", mode != null ? mode : "included");
        updates.put("geoRequirementLatitude", latitude);
        updates.put("geoRequirementLongitude", longitude);
        updates.put("geoRequirementRadiusKm", radiusKm);
        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}
