package com.example.eventlotterysystem.model;

import java.io.Serializable;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.ArrayList;
>>>>>>> 17fb52be6aec067650178567ba54a397b50dc29d

public class Event implements Serializable {
    private String eventId;
    private String title;
    private String description;
    private int capacity;
<<<<<<< HEAD
    private long registrationStartDate;   // timestamp (milliseconds)
    private long registrationEndDate;     // timestamp
    private int waitingListCount;         // number of users on waiting list
    private long date;
    private String place;
    private String privacy;              // For public/private
    private String org_id;
    private List<String> co_org_ids;
    // TODO: poster image URL
=======
    private long registrationStartDate;
    private long registrationEndDate;
    private int waitingListCount;
    private String category;
>>>>>>> 17fb52be6aec067650178567ba54a397b50dc29d

    // ✅ FIX: support multiple organizers
    private List<String> organizerIds;

    // Required no-arg constructor
    public Event() {
        organizerIds = new ArrayList<>();
    }

    public Event(String eventId, String title, String description) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.organizerIds = new ArrayList<>();
    }

<<<<<<< HEAD
    // Getters and setters

=======
    // Getters & setters
>>>>>>> 17fb52be6aec067650178567ba54a397b50dc29d
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

<<<<<<< HEAD
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setOrg_id(String org_id) {
        this.org_id = org_id;
    }

    public String getOrg_id() {
        return org_id;
    }

    public List<String> getCo_org_ids() {
        return co_org_ids;

    }
    public void add_co_org(String id){
        this.co_org_ids.add(id);
    }

    public void remove_co_org(String id){
        this.co_org_ids.remove(id);
    }
=======
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
>>>>>>> 17fb52be6aec067650178567ba54a397b50dc29d

    // ✅ NEW METHODS
    public List<String> getOrganizerIds() { return organizerIds; }

    public void setOrganizerIds(List<String> organizerIds) {
        this.organizerIds = organizerIds;
    }

    // Optional helper (clean filtering)
    public boolean isOrganizer(String userId) {
        return organizerIds != null && organizerIds.contains(userId);
    }
}