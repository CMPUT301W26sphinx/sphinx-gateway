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
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AcceptEventInviteFragment extends Fragment {
    /**
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout that contains the RecyclerView
        return inflater.inflate(R.layout.accept_event_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.event_invite_recycler_view);
        List<Event> filteredEvents = new ArrayList<>();
        EventInviteAdapter adapter = new EventInviteAdapter(filteredEvents);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        EventRepository repo = new EventRepository();
        EntrantListFirebase entrantDB = new EntrantListFirebase();
        ProfileManager profileManager = ProfileManager.getInstance();
        String currentUserId = profileManager.getUserID();

        // Load all events
        repo.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                // Check which events the user is invited to
                for (Event event : events) {
                    entrantDB.getEntrantStatus(event.getEventId(), currentUserId)
                            .addOnSuccessListener(status -> {
                                if (status == EntrantListEntry.STATUS_INVITED) {
                                    filteredEvents.add(event);
                                    adapter.notifyItemInserted(filteredEvents.size() - 1);
                                }
                            });
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });


    }


}
