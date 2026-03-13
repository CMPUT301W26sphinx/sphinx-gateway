package com.example.eventlotterysystem.model;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Event implements Serializable {
    // - Event Base? -
    private String eventID;

    // - Front Info Of The Event -
    private String eventName;
    private String eventDescription;
    private String eventPlace;
    private Date eventTime;
    private List<Date> registrationDate;

    private int capacity;

    // TODO: private something like photo event?


    public Event(){
        this.capacity = 0;
    }
    // Construct new Event
    public Event(String eventName, String eventDescription) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.capacity = 0;

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

    public List<Date> getRegistrationDate() {
        return registrationDate;
    }

    public int getCapacity() {
        return capacity;
    }

    // Setter


    public void setEventId(String eventID) {
        this.eventID = eventID;
    }

    public void setTitle(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String eventDescription) {
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

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}