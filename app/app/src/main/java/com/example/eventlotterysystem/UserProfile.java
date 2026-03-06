package com.example.eventlotterysystem;

import java.io.Serializable;

/**
 * Stores and manages the information for a user profile
 */
public class UserProfile implements Serializable {
    private String userID;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean receiveNotifications; // notification preference

    // private role userRole; //need role class, entrant, organizer, or whatever

    // Construct
    UserProfile(String userID) {
        this.userID = userID;
    }

    // Setters

    /**
     * Update the users name
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.name = userName;
    }

    /**
     * Update the users email
     *
     * @param userEmail
     */
    public void setUserEmail(String userEmail) {
        this.email = userEmail;
    }

    /**
     * Update the users phone number
     *
     * @param userPhone
     */
    public void setUserPhoneNumber(String userPhone) {
        this.phoneNumber = userPhone;
    }

    /**
     * Update the users notifcation preference
     *
     * @param choice
     */
    public void setReceiveNotifications(boolean choice) {
        this.receiveNotifications = choice;
    }

    // Getters

    /**
     * Get the users stored name
     *
     * @return
     */
    public String getUserName() {
        return name;
    }

    /**
     * Get the users userID (UID)
     *
     * @return
     */
    public String getProfileID() {
        return userID;
    }

    /**
     * Update the users stored phone number
     *
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Update the users stored email address
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns notification preference
     * @return
     */
    public boolean getNotificationPreference(){
        return receiveNotifications;
    }


}