package com.example.eventlotterysystem.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserCommentTest {
    // TODO: implement UserComment tests
    @Test
    public void CreateUserCommentTest(){
        UserComment comment = new UserComment("Hello World!", "uid", "John");
        assertEquals("John", comment.getUserName());
        assertEquals("uid", comment.getUserID());
        assertEquals("Hello World!", comment.getText());
    }
}
