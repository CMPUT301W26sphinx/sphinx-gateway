package com.example.eventlotterysystem.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserCommentTest {
    // TODO: implement UserComment tests
    @Test
    public void addUserCommentTest(){
        UserComment comment = new UserComment();
        // add the comment
        comment.setComment("Test");
        // get comment
        assertEquals("Test", comment.getComment());
    }
}
