package com.example.eventlotterysystem.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for EntrantDisplay.
 *
 * These tests check:
 * - default constructor values
 * - full constructor values
 * - getter/setter behavior
 * - edge cases for getFullName()
 */
public class EntrantDisplayTest {

    /**
     * Test that the default constructor has null
     */
    @Test
    public void testDefaultConstructor() {
        EntrantDisplay display = new EntrantDisplay();

        assertNull(display.getEntrantId());
        assertNull(display.getFirstName());
        assertNull(display.getLastName());
        assertNull(display.getEmail());
        assertEquals(0, display.getStatus());
    }

    /**
     * Test that the full constructor correctly assigns all fields.
     */
    @Test
    public void testFullConstructor() {
        EntrantDisplay display = new EntrantDisplay(
                "entrant1",
                "Jane",
                "Doe",
                "jane@example.com",
                2
        );

        assertEquals("entrant1", display.getEntrantId());
        assertEquals("Jane", display.getFirstName());
        assertEquals("Doe", display.getLastName());
        assertEquals("jane@example.com", display.getEmail());
        assertEquals(2, display.getStatus());
    }

    /**
     * Test setters and getters for all fields.
     */
    @Test
    public void testSettersAndGetters() {
        EntrantDisplay display = new EntrantDisplay();

        display.setEntrantId("entrant99");
        display.setFirstName("John");
        display.setLastName("Smith");
        display.setEmail("john@example.com");
        display.setStatus(5);

        assertEquals("entrant99", display.getEntrantId());
        assertEquals("John", display.getFirstName());
        assertEquals("Smith", display.getLastName());
        assertEquals("john@example.com", display.getEmail());
        assertEquals(5, display.getStatus());
    }

    /**
     * Test getFullName() when both first and last name exist.
     */
    @Test
    public void testGetFullName_bothNamesPresent() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("Jane");
        display.setLastName("Doe");

        assertEquals("Jane Doe", display.getFullName());
    }

    /**
     * Test getFullName() when only first name exists.
     */
    @Test
    public void testGetFullName_onlyFirstName() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("Jane");
        display.setLastName(null);

        assertEquals("Jane", display.getFullName());
    }

    /**
     * Test getFullName() when only last name exists.
     */
    @Test
    public void testGetFullName_onlyLastName() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName(null);
        display.setLastName("Doe");

        assertEquals("Doe", display.getFullName());
    }

    /**
     * Test getFullName() when both names are null.
     * Should return "Unknown".
     */
    @Test
    public void testGetFullName_bothNamesNull() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName(null);
        display.setLastName(null);

        assertEquals("Unknown", display.getFullName());
    }

    /**
     * Test getFullName() when both names are empty strings.
     * Should return "Unknown".
     */
    @Test
    public void testGetFullName_bothNamesEmpty() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("");
        display.setLastName("");

        assertEquals("Unknown", display.getFullName());
    }

    /**
     * Test getFullName() when first name is empty and last name is null.
     * Should return "Unknown"
     */
    @Test
    public void testGetFullName_emptyAndNull() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("");
        display.setLastName(null);

        assertEquals("Unknown", display.getFullName());
    }

    /**
     * Test getFullName() trims extra spaces when one side is empty.
     */
    @Test
    public void testGetFullName_trimsProperly() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("Jaylin");
        display.setLastName("");

        assertEquals("Jaylin", display.getFullName());
    }

    /**
     * Test that whitespace-only names are preserved by current implementation.
     *
     * Note: because trim() is used on the combined full name,
     * whitespace-only first and last names become empty and return "Unknown".
     */
    @Test
    public void testGetFullName_whitespaceOnlyNames() {
        EntrantDisplay display = new EntrantDisplay();
        display.setFirstName("   ");
        display.setLastName("   ");

        assertEquals("Unknown", display.getFullName());
    }

    /**
     * Test negative status value to confirm model stores it as given.
     * This does not validate business rules, only object behavior.
     */
    @Test
    public void testSetStatus_negativeValue() {
        EntrantDisplay display = new EntrantDisplay();
        display.setStatus(-1);

        assertEquals(-1, display.getStatus());
    }

    /**
     * Test unusual string values are stored exactly as given.
     */
    @Test
    public void testSetters_storeUnusualStrings() {
        EntrantDisplay display = new EntrantDisplay();

        display.setEntrantId("");
        display.setFirstName("J");
        display.setLastName("Auger-LaFleur!!");
        display.setEmail("not-an-email");

        assertEquals("", display.getEntrantId());
        assertEquals("J", display.getFirstName());
        assertEquals("Auger-LaFleur!!", display.getLastName());
        assertEquals("not-an-email", display.getEmail());
    }
}
