package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.OrganizerAdapter;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button createEventButton;
    private OrganizerAdapter adapter;
    private List<Event> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organizer_eventlist, container, false);

        recyclerView = view.findViewById(R.id.OrganizerEventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();

        adapter = new OrganizerAdapter(eventList, event -> {
            // 🔥 NAVIGATION FIX
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());

            OrganizerEventNavigationFragment fragment =
                    OrganizerEventNavigationFragment.newInstance(event.getEventId());

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        loadEvents();
        /**
         * Creates new event button, moved from original place.
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

        return view;
    }

    private void loadEvents() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("events")
                // 🔥 FIXED: supports multiple organizers
                .whereArrayContains("organizerIds", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    eventList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        eventList.add(event);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}