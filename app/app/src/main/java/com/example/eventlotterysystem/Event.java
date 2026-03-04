package com.example.eventlotterysystem;

import android.security.identity.IdentityCredentialStore;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    // - Event Base? -
    private Integer EventID;

    // - Front Info Of The Event -
    private String eventName;
    private String eventDescription;
    private int capacity;
    private Date registrationStartTime;

    // TODO: private something like photo event?

    // Construct new Event
    Event(String eventName) {
        this.eventName = eventName;
        this.eventDescription = "";
        this.capacity = 0; //baseline capacity, can be changed later

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
