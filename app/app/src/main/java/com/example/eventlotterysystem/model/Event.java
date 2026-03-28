package com.example.eventlotterysystem.model;

import java.io.Serializable;

public class Event implements Serializable {
    private String eventId;
    private String title;           // was eventName
    private String description;     // was eventDescription
    private int capacity;
    private long registrationStartDate;   // timestamp (milliseconds)
    private long registrationEndDate;     // timestamp
    private int waitingListCount;         // number of users on waiting list
    // TODO: poster image URL

    private String organizerId;

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    // Required no-arg constructor for Firestore
    public Event() {}

    public Event(String eventId, String title, String description) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
    }

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public long getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(long registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public long getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(long registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public int getWaitingListCount() { return waitingListCount; }
    public void setWaitingListCount(int waitingListCount) { this.waitingListCount = waitingListCount; }


}