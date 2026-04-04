package com.example.eventlotterysystem.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Event implements Serializable {
    private String eventId;
    private String title;
    private String description;
    private int capacity;
    private long registrationStartDate;   // timestamp (milliseconds)
    private long registrationEndDate;     // timestamp
    private int waitingListCount;         // number of users on waiting list
    private long date;
    private String place;
    private String privacy;              // For public/private
    // TODO: poster image URL
    private String category;

    // ✅ FIX: support multiple organizers
    private String organizerId;
    private List<String> co_organizerIds;

    // New field for Base64 image (admin branch)
    private String imageData;

    // Required no-arg constructor
    public Event() {
        this.co_organizerIds = new ArrayList<>();
    }

    public Event(String eventId, String title, String description) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.co_organizerIds = new ArrayList<>();
    }

    // Getters & setters
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // ✅ NEW METHODS
    public String getOrganizerId() { return organizerId; }

    public void setOrganizerId(String Id) {
        this.organizerId = Id;
    }

    public List<String> getCoOrganizerIds() { return co_organizerIds; }
    public void setCoOrganizerIds(List<String> ids) { this.co_organizerIds = ids; }
    public void addCoOrganizer(String Id) {
        this.co_organizerIds.add(Id);
    }

    // Optional helper (clean filtering)
    public boolean isOrganizer(String userId) {
        if (userId == null) return false;
        if (userId.equals(organizerId)) return true;
        return co_organizerIds != null && co_organizerIds.contains(userId);
    }

    // Image data (admin branch)
    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }
}