package com.example.eventlotterysystem.model;

/**
 * UI model for displaying an entrant on the organizer entrants screen.
 * helper to combine user profile info with event-specific entrant status. (entrant list entry)
 * @author Jaylin
 */
public class EntrantDisplay {

    private String entrantId;
    private String firstName;
    private String lastName;
    private String email;
    private int status;

    public EntrantDisplay() {
    }

    public EntrantDisplay(String entrantId, String firstName, String lastName, String email, int status) {
        this.entrantId = entrantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
    }

    public String getEntrantId() {
        return entrantId;
    }

    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
     }

    /**
     * Get the full name of the entrant.
     * If either first or last name is null, return "Unknown".
     * @return The full name of the entrant. Str
     */
    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Unknown" : full;
    }
}