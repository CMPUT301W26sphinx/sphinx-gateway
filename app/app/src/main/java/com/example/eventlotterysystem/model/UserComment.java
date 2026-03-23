package com.example.eventlotterysystem.model;


public class UserComment {
    /**
     * This defines the structure of a comment to be stored in an event
     */
    private String comment;

    // empty constructor for firebase
    public UserComment() {

    }

    // getters
    public String getComment() {
        return comment;
    }

    // setters
    public void setComment(String comment) {
        this.comment = comment;
    }

}
