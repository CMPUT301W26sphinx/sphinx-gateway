package com.example.eventlotterysystem.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserCommentTest {
    // TODO: implement UserComment tests
    @Test
    public void NewUserCommentTest(){
        UserComment comment = new UserComment("Hello World");
        // get comment
        assertEquals("Hello World", comment.getText());
    }
}
