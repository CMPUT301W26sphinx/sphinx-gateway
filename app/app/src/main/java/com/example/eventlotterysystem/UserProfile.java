package com.example.eventlotterysystem;

import java.io.Serializable;
import java.util.UUID;

public class UserProfile implements Serializable {
    private String profileID;
    private String userName;
    private String userEmail;
    private Long userPhone;

    // private role userRole; //need role class, entrant, organizer, or whatever
    private boolean notification_preference;

    // Construct
    UserProfile (String profileID){
        this.profileID = profileID;
    }

    // Setters
    public void setUserName (String userName){
        this.userName = userName;
    }
    public void setUserEmail (String userEmail){
        this.userEmail = userEmail;
    }
    public void setUserPhone (Long userPhone){
        this.userPhone = userPhone;
    }
    public void setNotification_preference(boolean choice){
        this.notification_preference = choice;
    }

    // Getters
    public String getUserName (){
        return userName;
    }
    public String getProfileID(){
        return profileID;
    }
}