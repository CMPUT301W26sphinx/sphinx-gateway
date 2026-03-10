package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.eventlotterysystem.model.profiles.UserProfile;

import org.junit.Test;

public class UserProfileTest {
    @Test
    public void testGetProfileID(){
        UserProfile userProfile = new UserProfile("qwerty");
        // test ID retrieval
        assertEquals("qwerty", userProfile.getProfileID());
    }
    @Test
    public void testGetUserName(){
        UserProfile userProfile = new UserProfile("qwerty");
        // set username
        userProfile.setUserName("Lorem Ipsum");
        // test retrieval
        assertEquals("Lorem Ipsum", userProfile.getUserName());
    }
    @Test
    public void testGetEmail(){
        UserProfile userProfile = new UserProfile("qwerty");
        // set email
        userProfile.setUserEmail("test@gmail.com");
        // test retrieval
        assertEquals("test@gmail.com", userProfile.getEmail());
    }
    @Test
    public void testGetPhoneNumber(){
        UserProfile userProfile = new UserProfile("qwerty");
        // set phone number
        userProfile.setUserPhoneNumber("780-777-3576");
        // test retrieval
        assertEquals("780-777-3576", userProfile.getPhoneNumber());
    }
    @Test
    public void testSetNotificationPreference(){
        UserProfile userProfile = new UserProfile("qwerty");
        // set preference
        userProfile.setReceiveNotifications(true);
        // test retrieval
        assertTrue(userProfile.getNotificationPreference());
    }
}
