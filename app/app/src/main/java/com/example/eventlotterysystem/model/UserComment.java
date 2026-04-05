package com.example.eventlotterysystem.model;

import com.google.firebase.Timestamp;

/**
 * Represents a comment made by a user on an event.
 */
public class UserComment {
    private String text;
    private Timestamp timestamp;
    private String userID;
    private String userName;
    private String commentID;
    private boolean isOrganizer;

    /**
     * Empty constructor required for Firebase deserialization.
     */
    public UserComment() {}

    /**
     * Creates a user comment with text, user ID, and user name.
     *
     * @param text the comment text
     * @param userID the ID of the user who made the comment
     * @param userName the name of the user who made the comment
     */
    public UserComment(String text, String userID, String userName) {
        this.text = text;
        this.userID = userID;
        this.userName = userName;
    }

    /**
     * Creates a user comment and records whether the commenter is an organizer.
     *
     * @param text the comment text
     * @param userID the ID of the user who made the comment
     * @param userName the name of the user who made the comment
     * @param isOrganizer whether the commenter is an organizer
     */
    public UserComment(String text, String userID, String userName, boolean isOrganizer) {
        this(text, userID, userName);
        this.isOrganizer = isOrganizer;
    }

    public boolean getIsOrganizer() { return isOrganizer; }
    public String getCommentID() { return commentID; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserID() { return userID; }
    public String getUserName() { return userName; }

    public void setIsOrganizer(boolean isOrganizer) { this.isOrganizer = isOrganizer; }
    public void setCommentID(String commentID) { this.commentID = commentID; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setUserName(String userName) { this.userName = userName; }
}