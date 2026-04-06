package com.example.eventlotterysystem.model;

import com.google.firebase.Timestamp;

/**
 * This class is used to represent a log entry.
 *
 */
public class Log {
    private String EntrantID;
    private String EventID;
    private String Message;
    private Timestamp Time;

    // Required no-arg constructor
    public Log() {}

    // Getters
    public String getEntrantID() { return EntrantID; }
    public String getEventID() { return EventID; }
    public String getMessage() { return Message; }
    public Timestamp getTime() { return Time; }

    // Setters
    public void setEntrantID(String entrantID) { this.EntrantID = entrantID; }
    public void setEventID(String eventID) { this.EventID = eventID; }
    public void setMessage(String message) { this.Message = message; }
    public void setTime(Timestamp time) { this.Time = time; }
}