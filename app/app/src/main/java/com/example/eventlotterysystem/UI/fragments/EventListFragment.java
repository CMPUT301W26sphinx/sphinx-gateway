package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.EventAdapter;
import com.example.eventlotterysystem.model.Event;
import com.google.android.material.button.MaterialButtonToggleGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying a list of events.
 *
 * <p>This fragment retrieves event data from Firebase Firestore and displays
 * the events using a RecyclerView with the EventAdapter.</p>
 *
 * <p>When a user selects an event, the fragment navigates to
 * {@link EventDetailsFragment} to display detailed information about
 * the selected event.</p>
 */
public class EventListFragment extends Fragment {

    /** RecyclerView used to display the list of events */
    RecyclerView recyclerView;

    /** Adapter used to bind event data to RecyclerView items */
    EventAdapter adapter;

    /** List that stores Event objects retrieved from Firestore */
    List<Event> eventList;

    /** Firebase Firestore instance used for retrieving event data */
    FirebaseFirestore db;

    /** Button to access create Event */
    private Button createEventButton;

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater used to inflate the layout
     * @param container Parent container
     * @param savedInstanceState Saved instance state
     * @return Inflated view for the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.eventlist_main, container, false);
    }

    /**
     * Called after the fragment view has been created.
     * Initializes RecyclerView, adapter, and retrieves event data from Firestore.
     *
     * @param view Fragment view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView and set layout manager
        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize event list
        eventList = new ArrayList<>();

        /**
         * Create adapter with click listener.
         * When an event is clicked, the app navigates to EventDetailsFragment.
         */
        adapter = new EventAdapter(eventList, event -> {

            // Pass selected event ID to details fragment
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getEventId());

            EventDetailsFragment detailsFragment = new EventDetailsFragment();
            detailsFragment.setArguments(bundle);

            // Replace current fragment with EventDetailsFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Attach adapter to RecyclerView
        recyclerView.setAdapter(adapter);

        // Initialize Firestore database instance
        db = FirebaseFirestore.getInstance();

        /**
         * Retrieve all events from the "events" collection in Firestore.
         * Each document is converted into an Event object.
         */
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        // Extract event information from Firestore document
                        String name = document.getString("title");
                        String description = document.getString("description");

                        // Create Event object using constructor compatible with Event.java
                        Event event = new Event(
                                document.getId(),
                                name,
                                description
                        );

                        // Add event to list
                        eventList.add(event);
                    }

                    // Notify adapter that data has changed
                    adapter.notifyDataSetChanged();
                });

        /**
         * Initialize toggle button group and set default selection.
         * Currently defaults to displaying all events.
         */
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.check(R.id.buttonAll);

        /**
         * For creating new event
         */
        createEventButton = view.findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(v -> {
            Fragment fragment = CreateEventFragment.newInstance();
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}