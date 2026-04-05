package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.eventlotterysystem.model.profiles.UserProfile;

import org.junit.Test;

public class UserProfileTest {
    @Test
    public void testGetProfileID(){
        UserProfile userProfile = new UserProfile();
        // set id
        userProfile.setUserID("qwerty");
        // test ID retrieval
        assertEquals("qwerty", userProfile.getUserID());
    }
    @Test
    public void testGetUserName(){
        UserProfile userProfile = new UserProfile();
        // set username
        userProfile.setFirstName("Lorem Ipsum");
        // test retrieval
        assertEquals("Lorem Ipsum", userProfile.getFirstName());
    }
    @Test
    public void testGetEmail(){
        UserProfile userProfile = new UserProfile();
        // set email
        userProfile.setEmail("test@gmail.com");
        // test retrieval
        assertEquals("test@gmail.com", userProfile.getEmail());
    }
    @Test
    public void testGetPhoneNumber(){
        UserProfile userProfile = new UserProfile();
        // set phone number
        userProfile.setPhoneNumber("780-777-3576");
        // test retrieval
        assertEquals("780-777-3576", userProfile.getPhoneNumber());
    }
    @Test
    public void testSetNotificationPreference(){
        UserProfile userProfile = new UserProfile();
        // set preference
        userProfile.setNotificationPreference(true);
        // test retrieval
        assertTrue(userProfile.getNotificationPreference());
    }
}
