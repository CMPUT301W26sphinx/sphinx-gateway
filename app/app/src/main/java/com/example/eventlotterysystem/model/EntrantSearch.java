package com.example.eventlotterysystem.model;

import java.util.ArrayList;

/**
 * This class contains methods for the filtering of events
 */
public class EntrantSearch {
    /**
     * Filters events by if the description contains a keyword
     * @param events the unfiltered list
     * @param keyword the desired word to search the description for
     * @return a filtered list of events that contain the keyword
     */
    public ArrayList<Event> filterEventsByKeyword(ArrayList<Event> events, String keyword){
        ArrayList<Event> filteredEvents = new ArrayList<>();
        for(Event event : events){
            if(event.getDescription().contains(keyword)){
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }
}
