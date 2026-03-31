package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class OrganizerCommentsFragment extends Fragment {
    private static final String EVENT_ID = "event_id";

    /**
     * Create Event Details fragment for specific event with eventId
     *
     * @param eventId The unique identifier for the event
     * @return fragment
     * A new instance of EventDetailsFragment
     */
    public static OrganizerCommentsFragment newInstance(String eventId) {
        /*
         Author: RobinHood https://stackoverflow.com/users/646806/robinhood
         Title: "How can I transfer data from one fragment to another fragment android"
         Answer: https://stackoverflow.com/a/19333288
         Date: Oct 12, 2013
         */
        OrganizerCommentsFragment fragment = new OrganizerCommentsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }
}
