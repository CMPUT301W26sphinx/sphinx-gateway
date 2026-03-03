package com.example.eventlotterysystem.model;

/**
 * This class is used to represent an entry in the waitlist for an event.
 *Stored in Firestore
 */
public class WaitlistEntry {
    private String eventId;
    private String entrantId;

    private String status; // for invited, etc before going to registered status

    /**
     * empty consructor used by firestore
     */
    public WaitlistEntry() {
    }
    /**
     * constructor used by app to create a new waitlist entry
     * @param eventId
     *  The id of the event to add to the waitlist for.
     * @param entrantId
     *  The id of the entrant to add to the waitlist.
     */
    public WaitlistEntry(String eventId, String entrantId) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = "WAITLIST";
    }
    /**
     * constructor used by app to create a new waitlist entry
     * @param eventId
     *  The id of the event to add to the waitlist for.
     * @param entrantId
     *  The id of the entrant to add to the waitlist.
     * @param status
     *  The status of the waitlist entry.(WAITLIST, INVITED, etc.)
     */
    public WaitlistEntry(String eventId, String entrantId, String status) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = status;
    }

    // getters
    /**
     * This gets the id of the entrant to add to the waitlist.
     * @return eventId
     *  The id of the event to add to the waitlist for.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * This gets the id of the entrant to add to the waitlist.
     * @return entrantId
     *  The id of the entrant to add to the waitlist.
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * This gets the status of the waitlist entry.(WAITLIST, INVITED, etc.)
     * @return status
     *  The status of the waitlist entry.(WAITLIST, INVITED, etc.)
     */
    public String getStatus() {
        return status;
    }

    // setters
    /**
     * This sets the status of the waitlist entry.(WAITLIST, INVITED, etc.)
     * @param status
     *  The status of the waitlist entry.(WAITLIST, INVITED, etc.)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This sets the id of the event to add to the waitlist for.
     * @param eventId
     *  The id of the event to add to the waitlist for.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * This sets the id of the entrant to add to the waitlist.
     * @param entrantId
     *  The id of the entrant to add to the waitlist.
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

}
