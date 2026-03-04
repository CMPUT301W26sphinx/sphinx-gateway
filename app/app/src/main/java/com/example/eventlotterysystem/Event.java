package com.example.eventlotterysystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Event {
    private String eventId;
    private String name;
    private String description;
    private String time;
    private String place;
    private String posterUrl;
    private String qrUrl;
    private List<String> period; // [start, end]
    private String organizerId;
    private List<String> entrants;
    private List<String> attendees;
    private Integer maxEntrants; // null = infinity
    public Event() {
    }
    public Event(String eventId,
                 String name,
                 String description,
                 String time,
                 String place,
                 String posterUrl,
                 String qrUrl,
                 List<String> period,
                 String organizerId,
                 Integer maxEntrants
    ) {

        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.time = time;
        this.place = place;
        this.posterUrl = posterUrl;
        this.qrUrl = qrUrl;
        if (period == null) {
            this.period = Arrays.asList(null, null);
        } else {
            this.period = period;
        }
        this.organizerId = organizerId;
        this.entrants = new ArrayList<>();
        this.attendees = new ArrayList<>();
        this.maxEntrants = maxEntrants;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPlace() {
        return place;
    }

    public String getTime() {
        return time;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public List<String> getPeriod() {
        return period;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public List<String> getEntrants() {
        return entrants;
    }

    public Integer getMaxEntrants() {
        return maxEntrants;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }
}
