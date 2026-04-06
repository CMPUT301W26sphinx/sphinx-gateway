package com.example.eventlotterysystem.UI.fragments.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminEventDetailActivity;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is used to show events in Admin view
 * @author Hassan
 */
public class AdminEventsFragment extends Fragment {

    private static final String TAG = "AdminEventsFragment";

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private final List<Event> filteredEvents = new ArrayList<>(); // displayed list
    private List<Event> allEvents = new ArrayList<>(); // full list from DB
    private EventRepository repository;

    private View filterAction;
    private TextView activeFilterChip;
    private String currentFilter = null; // null means no filter

    public AdminEventsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_events, container, false);

        recyclerView = view.findViewById(R.id.events_recycler);
        filterAction = view.findViewById(R.id.filter_action);
        activeFilterChip = view.findViewById(R.id.active_filter_chip);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EventAdapter(filteredEvents, this::openEventDetail);
        recyclerView.setAdapter(adapter);

        // Filter click – open search dialog
        filterAction.setOnClickListener(v -> showFilterDialog());

        // Clear filter chip click
        activeFilterChip.setOnClickListener(v -> clearFilter());

        repository = new EventRepository();
        loadEvents();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents(); // refresh when returning
    }

    private void loadEvents() {
        Log.d(TAG, "Fetching events from Firestore...");

        repository.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                Log.d(TAG, "Events loaded: " + events.size());

                if (getActivity() == null) return;

                allEvents.clear();
                allEvents.addAll(events);

                // Apply current filter if any
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    filter(currentFilter);
                } else {
                    showAll();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading events", e);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter events");

        final EditText input = new EditText(requireContext());
        input.setHint("Search by name or description");
        builder.setView(input);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                currentFilter = query;
                filter(query);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void filter(String query) {
        String lowerQuery = query.toLowerCase();
        filteredEvents.clear();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(lowerQuery) ||
                    event.getDescription().toLowerCase().contains(lowerQuery)) {
                filteredEvents.add(event);
            }
        }
        adapter.notifyDataSetChanged();

        // Show chip with query
        activeFilterChip.setVisibility(View.VISIBLE);
        activeFilterChip.setText("Filter: " + query + " ✕");
    }

    private void clearFilter() {
        currentFilter = null;
        showAll();
        activeFilterChip.setVisibility(View.GONE);
    }

    private void showAll() {
        filteredEvents.clear();
        filteredEvents.addAll(allEvents);
        adapter.notifyDataSetChanged();
    }

    private void openEventDetail(Event event) {
        Intent intent = new Intent(getActivity(), AdminEventDetailActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        startActivity(intent);
    }
}