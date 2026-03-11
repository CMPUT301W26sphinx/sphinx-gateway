package com.example.eventlotterysystem.model;

/**
 * This class is used to represent an entry in the waitlist for an event.
 *Stored in Firestore
 */
public class EntrantListEntry {
    public static final int STATUS_WAITLIST = 1;
    public static final int STATUS_INVITED = 2;
    public static final int STATUS_REGISTERED = 3;
    public static final int STATUS_CANCELLED_OR_REJECTED = 4;

    private String eventId;
    private String entrantId;

    private int status; // for invited, etc before going to registered status

    /**
     * empty consructor used by firestore
     */
    public EntrantListEntry() {
    }
    /**
     * constructor used by app to create a new waitlist entry
     * @param eventId
     *  The id of the event to add to the waitlist for.
     * @param entrantId
     *  The id of the entrant to add to the waitlist.
     */
    public EntrantListEntry(String eventId, String entrantId) {
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = STATUS_WAITLIST;
    }
    /**
     * constructor used by app to create a new waitlist entry
     * @param eventId
     *  The id of the event to add to the waitlist for.
     * @param entrantId
     *  The id of the entrant to add to the waitlist.
     * @param status
     *  The status of the waitlist entry.(1-WAITLIST, 2-INVITED, etc.)
     */
    public EntrantListEntry(String eventId, String entrantId, int status) {
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
    public int getStatus() {
        return status;
    }

    // setters
    /**
     * This sets the status of the waitlist entry.(WAITLIST, INVITED, etc.)
     * @param status
     *  The status of the waitlist entry.(WAITLIST, INVITED, etc.)
     */
    public void setStatus(int status) {
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
