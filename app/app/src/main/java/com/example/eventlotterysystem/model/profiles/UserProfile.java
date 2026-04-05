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
    private List<String> notification;// was Notifications
    private Double longitude;
    private Double latitude;

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

    // getters


    public String getUserID() {
        return userID;
    }

    public void setLatitude(Double latitude){this.latitude = latitude;}

    public void setLongitude(Double longitude){this.longitude = longitude;}

    // Getters
    /**
     * Get the users stored name
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean getNotificationPreference() {
        return notificationPreference;
    }

    public List<String> getNotification() {
        return notification;
    }

    // setters

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setNotificationPreference(boolean notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public void setNotification(List<String> notification) {
        this.notification = notification;
    }
    public Double getLatitude(){return latitude;}

    public Double getLongitude(){return longitude;}


}