package com.example.eventlotterysystem.model;

import com.google.firebase.Timestamp;

public class UserComment {
    private String text;
    private Timestamp timestamp;
    private String userID;
    private String userName;
    private String commentID;
    private boolean isOrganizer;

    // empty constructor for firebase
    public UserComment() {}

    // constructor used by main branch
    public UserComment(String text, String userID, String userName) {
        this.text = text;
        this.userID = userID;
        this.userName = userName;
    }

    // additional constructor for admin branch (with isOrganizer)
    public UserComment(String text, String userID, String userName, boolean isOrganizer) {
        this(text, userID, userName);
        this.isOrganizer = isOrganizer;
    }

    // Getters
    public boolean getIsOrganizer() { return isOrganizer; }
    public String getCommentID() { return commentID; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserID() { return userID; }
    public String getUserName() { return userName; }

    // Setters
    public void setIsOrganizer(boolean isOrganizer) { this.isOrganizer = isOrganizer; }
    public void setCommentID(String commentID) { this.commentID = commentID; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setUserName(String userName) { this.userName = userName; }
}