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
import com.example.eventlotterysystem.UI.adapters.EventInviteAdapter;
import com.example.eventlotterysystem.model.Event;

import java.util.ArrayList;
import java.util.List;

public class AcceptEventInviteFragment extends Fragment {
    /**
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout that contains the RecyclerView
        return inflater.inflate(R.layout.accept_event_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.event_invite_recycler_view);

        // Example dummy data
        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event("Hackathon", "24 hour coding event", "test"));
        eventList.add(new Event("Club Meeting", "Weekly discussion", "test"));

        // Adapter
        EventInviteAdapter adapter = new EventInviteAdapter(eventList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


    }
}
