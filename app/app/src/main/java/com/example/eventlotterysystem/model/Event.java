package com.example.eventlotterysystem.model;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Event implements Serializable {
    // - Event Base? -
    private static final AtomicInteger count = new AtomicInteger(1);
    private String eventID;


    // - Front Info Of The Event -
    private String eventName;
    private String eventDescription;
    private String eventPlace;
    private Date eventTime;
    private List<Date> registrationDate;

    private Double capacity;

    // TODO: private something like photo event?

    // Construct new Event
    public Event(String eventName, String eventDescription) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.capacity = Double.POSITIVE_INFINITY;

    }

    // Getters

    public String getEventID() {
        return eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public List<Date> getRegistrationStartDate() {
        return registrationDate;
    }

    public Double getCapacity() {
        return capacity;
    }

    // Setter

    public void setEventId(String eventId) {
        this.eventID = eventId;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public void setRegistrationDate(List<Date> registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }
}