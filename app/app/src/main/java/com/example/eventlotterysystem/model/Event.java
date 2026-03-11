package com.example.eventlotterysystem.model;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Event implements Serializable {
    // - Event Base? -
    private static final AtomicInteger count = new AtomicInteger(1);
    private int eventID;


    // - Front Info Of The Event -
    private String eventName;
    private String eventDescription;
    private String eventPlace;
    private Date eventTime;
    private List<Date> registrationStartDate;

    private Double capacity;

    // TODO: private something like photo event?

    // Construct new Event
    Event(String eventName) {
        this.eventName = eventName;
        this.capacity = Double.POSITIVE_INFINITY;

    }

    // Getters

    public int getEventID() {
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
        return registrationStartDate;
    }

    public Double getCapacity() {
        return capacity;
    }

    // Setter

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public void setRegistrationStartDate(List<Date> registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }
}