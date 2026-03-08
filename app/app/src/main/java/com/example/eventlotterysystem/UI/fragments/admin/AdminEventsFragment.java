package com.example.eventlotterysystem.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.activity.admin.AdminEventDetailActivity;

/**
 * Fragment that displays the admin Events page.
 * For now, it shows a few sample events using buttons.
 * Clicking an event opens the event detail screen.
 */
public class AdminEventsFragment extends Fragment {

    /**
     * Required empty constructor for the fragment.
     */
    public AdminEventsFragment() {
    }

    /**
     * Creates the Events fragment UI and connects the event buttons.
     * Each button currently opens the detail page using temporary sample data.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Load the XML layout for the admin events page.
        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        // Connect the event buttons from the layout.
        Button event1 = view.findViewById(R.id.event_button_1);
        Button event2 = view.findViewById(R.id.event_button_2);
        Button event3 = view.findViewById(R.id.event_button_3);

        // Open the detail page with temporary event data when a button is clicked.
        event1.setOnClickListener(v -> openEventDetail("Swimming Lessons", "Beginner swimming lessons for children."));
        event2.setOnClickListener(v -> openEventDetail("Piano Lessons", "Introductory piano lessons for beginners."));
        event3.setOnClickListener(v -> openEventDetail("Dance Class", "Basic interpretive dance and movement safety."));

        return view;
    }

    /**
     * Opens the admin event detail screen and sends the selected event data.
     *
     * @param title the title of the selected event
     * @param description the description of the selected event
     */
    private void openEventDetail(String title, String description) {
        Intent intent = new Intent(getActivity(), AdminEventDetailActivity.class);
        intent.putExtra("eventTitle", title);
        intent.putExtra("eventDescription", description);
        startActivity(intent);
    }
}