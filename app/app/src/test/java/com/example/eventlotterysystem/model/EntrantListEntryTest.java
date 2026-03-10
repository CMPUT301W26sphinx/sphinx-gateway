package com.example.eventlotterysystem.model;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is used to test the WaitlistEntry class.
 */
public class EntrantListEntryTest {
    /**
     * This method is used to test the WaitlistEntry class.
     * No parameters or returns.
     */
    public void testWaitlistEntry() {
        EntrantListEntry entry = new EntrantListEntry("123", "456");
        assertEquals("123", entry.getEventId());
        assertEquals("456", entry.getEntrantId());
        assertEquals(1, entry.getStatus());
    }

    /**
     * This method is used to test the setters for the WaitlistEntry class. With the default status of WAITLIST.
     * No parameters or returns.
     */
    public void testSettersGetters() {
        EntrantListEntry entry = new EntrantListEntry();
        entry.setEventId("789");
        entry.setEntrantId("012");
        entry.setStatus(2);

        assertEquals("789", entry.getEventId());
        assertEquals("012", entry.getEntrantId());
        assertEquals(2, entry.getStatus());
    }

    public void testConstructor() {
        EntrantListEntry entry = new EntrantListEntry("123", "456");
        assertEquals("123", entry.getEventId());
        assertEquals("456", entry.getEntrantId());
        assertEquals(1, entry.getStatus()); // default

        entry = new EntrantListEntry("789", "012", 2);
        assertEquals("789", entry.getEventId());
        assertEquals("012", entry.getEntrantId());
        assertEquals(2, entry.getStatus());

    }
}
