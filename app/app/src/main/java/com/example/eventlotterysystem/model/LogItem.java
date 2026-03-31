package com.example.eventlotterysystem.model;

public class LogItem {
    private String eventName;
    private String recipientName;
    private String message;
    private String timestamp; // formatted

    public LogItem(String eventName, String recipientName, String message, String timestamp) {
        this.eventName = eventName;
        this.recipientName = recipientName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getEventName() { return eventName; }
    public String getRecipientName() { return recipientName; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}