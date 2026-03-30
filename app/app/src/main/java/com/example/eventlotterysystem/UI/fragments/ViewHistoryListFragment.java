package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.EventAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewHistoryListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewHistoryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private EventAdapter adapter;
    private List<Event> waitlistedEvents = new ArrayList<>();

    private EventRepository eventRepository;
    private EntrantListFirebase entrantListFirebase;

    public ViewWaitListFragment() {}

    @Override
    /**
     * Allows the user to view the current events in which they are on the waiting list
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view of waiting list
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_wait_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        emptyText = view.findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(waitlistedEvents, this::onEventClick);
        recyclerView.setAdapter(adapter);
        eventRepository = new EventRepository();
        entrantListFirebase = new EntrantListFirebase();
        loadWaitlist();
    }

    /**
     * Loads the waitlisted events for the current user
     * @return void
     */
    private void loadWaitlist() {
        String currentUserId;
        try {currentUserId = ProfileManager.getInstance().getUserID();
        } catch (IllegalStateException e) {showEmptyState();return;} //if fail show empty, dont crash
        eventRepository.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                waitlistedEvents.clear();
                if (events.isEmpty()) {updateDisplay();return;}
                final int[] processed = {0};// make sure check all events
                // if status=1, then the entrant is on the waitlist, should be shown
                for (Event event : events) {
                    entrantListFirebase.getEntrantStatus(event.getEventId(), currentUserId).addOnSuccessListener(status -> {if (status != null && status == 1) {waitlistedEvents.add(event);}
                        processed[0]++;
                        if (processed[0] == events.size()) {updateDisplay();}}).addOnFailureListener(e -> {processed[0]++;if (processed[0] == events.size()) {updateDisplay();}});}
            }

            @Override
            public void onError(Exception e) {
                showEmptyState();
            }
        });
    }

    private void updateDisplay() {
        adapter.notifyDataSetChanged();
        if (waitlistedEvents.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void showEmptyState() {
        waitlistedEvents.clear();
        adapter.notifyDataSetChanged();
        emptyText.setVisibility(View.VISIBLE);
    }

    /**
     * Handles click on an event item.
     * From the initial EventListFragment, written by Hammad
     * @param event
     */
    private void onEventClick(Event event) {
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
    }
}
