package com.example.eventlotterysystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains methods for the filtering of events
 */
public class EntrantSearch {
    /**
     * Filters events by if the description contains a keyword
     *
     * @param events  the unfiltered list
     * @param keyword the desired word to search the description for
     * @return a filtered list of events that contain the keyword
     */
    public List<Event> filterEventsByKeyword(List<Event> events, String keyword) {
        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getDescription() != null && keyword != null && event.getDescription().toLowerCase().contains(keyword.toLowerCase())) {

                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }
}
