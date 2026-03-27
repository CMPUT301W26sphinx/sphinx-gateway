package com.example.eventlotterysystem.model;


import com.google.firebase.Timestamp;
public class UserComment {
    /**
     * This defines the structure of a comment to be stored in an event
     */
    private String text;
    private Timestamp timestamp;
    private String userID;
    private String userName;

    // empty constructor for firebase
    public UserComment() {

    }


    // getters
    public String getText() {
        return text;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    // setters
    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
