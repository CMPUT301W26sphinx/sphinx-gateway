package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.OrganizerAdapter;
import com.example.eventlotterysystem.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrganizerAdapter adapter;
    private List<Event> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organizer_eventlist, container, false);

        // ✅ STEP 1: Setup RecyclerView
        recyclerView = view.findViewById(R.id.OrganizerEventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ STEP 2: Initialize list + adapter
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

        // ✅ STEP 3: Load events
        loadEvents();

        return view;
    }

    private void loadEvents() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<Event> mergedEvents = new ArrayList<>();

        Task<QuerySnapshot> organizerQuery = db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .get();

        Task<QuerySnapshot> coOrganizerQuery = db.collection("events")
                .whereArrayContains("co_organizerIds", currentUserId)
                .get();

        Tasks.whenAllSuccess(organizerQuery, coOrganizerQuery)
                .addOnSuccessListener(results -> {
                    Set<String> seenIds = new HashSet<>();
                    eventList.clear();

                    for (Object result : results) {
                        QuerySnapshot snapshot = (QuerySnapshot) result;
                        for (QueryDocumentSnapshot doc : snapshot) {
                            if (seenIds.add(doc.getId())) { // deduplicates
                                Event event = doc.toObject(Event.class);
                                eventList.add(event);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}