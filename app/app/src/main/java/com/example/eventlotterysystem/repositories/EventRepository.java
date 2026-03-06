package com.example.eventlotterysystem.repositories;

import com.example.eventlotterysystem.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class responsible for providing event data.
 * For now, this uses temporary hardcoded data.
 * Later, this class can be updated to load events from Firestore
 * without requiring major changes to the UI code.
 */
public class EventRepository {

    /**
     * Returns a list of sample events for the admin Events page.
     *
     * @return a list of Event objects
     */
    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        // Temporary sample data used until Firestore is connected.
        events.add(new Event("Swimming Lessons", "Beginner swimming lessons for children."));
        events.add(new Event("Piano Lessons", "Introductory piano lessons for beginners."));
        events.add(new Event("Dance Class", "Basic interpretive dance and movement safety."));

        return events;
    }
}