package com.example.eventlotterysystem.models;

/**
 * Represents an event in the Event Lottery System.
 * This class stores the basic event information used by the app.
 */
public class Event {

    // Title of the event.
    private String title;

    // Short description of the event.
    private String description;

    /**
     * Required empty constructor.
     * This can be useful for Firebase or other tools that need a no-argument constructor.
     */
    public Event() {
    }

    /**
     * Creates an event with a title and description.
     *
     * @param title the event title
     * @param description the event description
     */
    public Event(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * Returns the title of the event.
     *
     * @return the event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description of the event.
     *
     * @return the event description
     */
    public String getDescription() {
        return description;
    }
}