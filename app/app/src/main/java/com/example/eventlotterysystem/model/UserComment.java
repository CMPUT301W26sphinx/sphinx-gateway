package com.example.eventlotterysystem.model;

import com.google.firebase.Timestamp;

public class UserComment {
    private String text;
    private Timestamp timestamp;
    private String userID;
    private String userName;
    private boolean isOrganizer;
    private String commentId;   // transient – not stored in Firestore

    // Required empty constructor for Firestore
    public UserComment() {}

    public UserComment(String text, String userID, String userName, boolean isOrganizer) {
        this.text = text;
        this.userID = userID;
        this.userName = userName;
        this.isOrganizer = isOrganizer;
    }

    // Getters
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserID() { return userID; }
    public String getUserName() { return userName; }
    public boolean isOrganizer() { return isOrganizer; }
    public String getCommentId() { return commentId; }

    // Setters
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
}