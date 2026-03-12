package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class EventListFragment extends Fragment {

    RecyclerView recyclerView;
    EventAdapter adapter;
    List<Event> eventList;

    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.eventlist_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();

        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Fetch events from Firestore
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String name = document.getString("name");
                        String description = document.getString("description");

                        Event event = new Event(name);
                        event.setEventDescription(description);

                        eventList.add(event);
                    }

                    adapter.notifyDataSetChanged();
                });

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.check(R.id.buttonAll);
    }
}