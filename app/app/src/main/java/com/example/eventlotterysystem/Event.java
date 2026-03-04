package com.example.eventlotterysystem;

import java.io.Serializable;
import java.time.DateTimeException;
import java.util.Date;
import java.util.UUID;

public class Event implements Serializable {
    // - Event Base? -
    private String eventID;
    // - Front Info Of The Event -
    private String eventName;
    private String eventDescription;
    private int capacity;
    private Date registrationStartDate;
    private DateTimeException registrationStartTime;
    // TODO: private something like photo event?

    // Construct new Event
    Event(String eventName) {
        this.eventName = eventName;
        this.eventDescription = "";
        this.capacity = 0; //baseline capacity, can be changed later
        this.eventID = UUID.randomUUID().toString(); //change it later? random uuid generator according to java doc
    }

    // Getters
    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return eventDescription;
    }

    public Integer getCapacity() {
        return capacity;
    }
    public String getEventID() {
        return eventID;
        }

    // Setters
    public void setName(String name) {
        this.eventName = name;
    }
    public void setEventDescription(String description){
        this.eventDescription = description;
    }
    public void setCapacity(int capacity){
        this.capacity = capacity;
    }
}
