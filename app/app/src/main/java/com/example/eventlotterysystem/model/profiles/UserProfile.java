package com.example.eventlotterysystem.model.profiles;

import java.io.Serializable;
import java.util.List;

/**
 * Stores and manages the information for a user profile
 * @author Noah Zapisocki
 */
public class UserProfile implements Serializable {
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean notificationPreference; // was receiveNotifications
    private List<String> notification;      // was Notifications

    // private role userRole; //need role class, entrant, organizer, or whatever

    // Construct
    public UserProfile() {

    }

    public UserProfile(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Setters

    /**
     * Update the users first name
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
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
     * Set the users last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Set user ID
     * @param userID
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    // Getters
    /**
     * Get the users stored name
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
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
    public boolean getNotificationPreference() {return notificationPreference;}

    /**
     * Sets notification preference
     * @param choice yes or no
     */
    public void setReceiveNotifications(boolean choice) {this.notificationPreference = choice;}
    /**
     * Gets notifications
     * @return notification
     */
    public List<String> getNotification() {return notification;}
    /**
     * Get the users last name.
     */
    public String getLastName() {
        return lastName;
    }

}