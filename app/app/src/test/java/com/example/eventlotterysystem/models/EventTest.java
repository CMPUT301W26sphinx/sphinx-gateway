package com.example.eventlotterysystem.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventTest {

    @Test
    public void eventConstructorStoresTitleCorrectly() {
        Event event = new Event("Swimming Lessons", "Beginner swimming lessons");
        assertEquals("Swimming Lessons", event.getTitle());
    }

    @Test
    public void eventConstructorStoresDescriptionCorrectly() {
        Event event = new Event("Swimming Lessons", "Beginner swimming lessons");
        assertEquals("Beginner swimming lessons", event.getDescription());
    }
}