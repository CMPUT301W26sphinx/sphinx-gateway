package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.UI.adapters.EventAdapter;
import com.example.eventlotterysystem.model.Event;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class EventListFragment extends Fragment {

    RecyclerView recyclerView;
    EventAdapter adapter;
    List<Event> eventList;

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

        Event e1 = new Event("Hackathon");
        e1.setEventDescription("Coding competition");
        eventList.add(e1);

        Event e2 = new Event("Workshop");
        e2.setEventDescription("Android workshop");
        eventList.add(e2);

        Event e3 = new Event("Seminar");
        e3.setEventDescription("Tech talk");
        eventList.add(e3);

        Event e4 = new Event("Career Fair");
        e4.setEventDescription("Meet recruiters and companies");
        eventList.add(e4);

        Event e5 = new Event("AI Conference");
        e5.setEventDescription("Discuss the latest in AI");
        eventList.add(e5);

        Event e6 = new Event("Startup Pitch");
        e6.setEventDescription("Students pitch startup ideas");
        eventList.add(e6);

        Event e7 = new Event("Game Jam");
        e7.setEventDescription("Build a game in 24 hours");
        eventList.add(e7);

        Event e8 = new Event("Networking Night");
        e8.setEventDescription("Connect with professionals");
        eventList.add(e8);

        Event e9 = new Event("Cybersecurity Talk");
        e9.setEventDescription("Learn about modern security threats");
        eventList.add(e9);

        Event e10 = new Event("Data Science Meetup");
        e10.setEventDescription("Discuss data analysis and ML");
        eventList.add(e10);

        Event e11 = new Event("Cloud Computing Workshop");
        e11.setEventDescription("Hands-on with AWS and Azure");
        eventList.add(e11);

        Event e12 = new Event("Design Thinking Session");
        e12.setEventDescription("Creative problem solving workshop");
        eventList.add(e12);

        adapter = new EventAdapter(eventList);

        recyclerView.setAdapter(adapter);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.check(R.id.buttonAll);

    }
}