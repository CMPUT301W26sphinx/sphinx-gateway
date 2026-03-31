package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.EntrantAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.model.EntrantDisplay;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.database.ProfileManager;

import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 * This fragment allows an organizer to view the entrants for an event according to their status
 * Use the {@link OrganizerEventEntrantsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerEventEntrantsFragment extends Fragment {

    private RecyclerView waitlistRecyclerView;
    private RecyclerView selectedRecyclerView;
    private RecyclerView enrolledRecyclerView;
    private RecyclerView cancelledRecyclerView;

    private EntrantAdapter waitlistAdapter;
    private EntrantAdapter selectedAdapter;
    private EntrantAdapter enrolledAdapter;
    private EntrantAdapter cancelledAdapter;

    private Button notifyWaitlistButton;
    private Button notifySelectedButton;
    private Button notifyCancelledButton;
    private Button exportCsvButton;
    private List<EntrantDisplay> enrolledList = new ArrayList<>();

    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
    private final ProfileManager profileManager = ProfileManager.getInstance();

    public OrganizerEventEntrantsFragment() {
        // Required empty public constructor
    }

    public static OrganizerEventEntrantsFragment newInstance(String eventId) {
        OrganizerEventEntrantsFragment fragment = new OrganizerEventEntrantsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_org_event_entrants, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        waitlistRecyclerView = view.findViewById(R.id.waitlistRecyclerView);
        selectedRecyclerView = view.findViewById(R.id.selectedRecyclerView);
        enrolledRecyclerView = view.findViewById(R.id.enrolledRecyclerView);
        cancelledRecyclerView = view.findViewById(R.id.cancelledRecyclerView);

        notifyWaitlistButton = view.findViewById(R.id.notifyWaitlistButton);
        notifySelectedButton = view.findViewById(R.id.notifySelectedButton);
        notifyCancelledButton = view.findViewById(R.id.notifyCancelledButton);
        exportCsvButton = view.findViewById(R.id.exportCsvButton);
        // TODO: button functionalities, notifs and the csv export
        exportCsvButton.setOnClickListener(v -> exportCsv());


        setupRecyclerViews();

        String eventId = null;
        if (getArguments() != null) {eventId = getArguments().getString("eventId");}
        if (eventId == null || eventId.isEmpty()) {Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_SHORT).show();return;}
        loadEntrants(eventId);

    }

    /**
     * This method is used to load the entrants for the event.
     *  No parameters or returns.
     */
    private void setupRecyclerViews() {
        // I read about the multiple recycler views here, https://www.geeksforgeeks.org/android/how-to-create-a-nested-recyclerview-in-android/
        waitlistAdapter = new EntrantAdapter();
        selectedAdapter = new EntrantAdapter();
        enrolledAdapter = new EntrantAdapter();
        cancelledAdapter = new EntrantAdapter();

        // Set up layout managers for each RecyclerView
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        enrolledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cancelledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        waitlistRecyclerView.setAdapter(waitlistAdapter);
        selectedRecyclerView.setAdapter(selectedAdapter);
        enrolledRecyclerView.setAdapter(enrolledAdapter);
        cancelledRecyclerView.setAdapter(cancelledAdapter);

            }

    /**
     * This method is used to load the entrants for the event.
     * @param eventId
     *  The id of the event to load the entrants for.
     *  No parameters or returns.
     */
    private void loadEntrants(String eventId){
        entrantListFirebase.getEntrantList(eventId)
                .addOnSuccessListener(entries -> {
                    if (!isAdded()) return;
                    if (entries == null || entries.isEmpty()) {
                        waitlistAdapter.setEntrants(new ArrayList<>());
                        selectedAdapter.setEntrants(new ArrayList<>());
                        enrolledAdapter.setEntrants(new ArrayList<>());
                        cancelledAdapter.setEntrants(new ArrayList<>());
                        return;
                    }

                    List<EntrantDisplay> waitlist = new ArrayList<>();
                    List<EntrantDisplay> selected = new ArrayList<>();
                    List<EntrantDisplay> enrolled = new ArrayList<>();
                    List<EntrantDisplay> cancelled = new ArrayList<>();

                    final int totalEntries = entries.size();
                    final int[] completedCount = {0}; // count helps with the firebase delay loading stuff in

                    for (EntrantListEntry entry : entries) {
                        profileManager.getUserProfileById(entry.getEntrantId(), user -> {
                            if (!isAdded()) return;

                            EntrantDisplay display = new EntrantDisplay(
                                    entry.getEntrantId(),
                                    user != null ? user.getFirstName() : null,
                                    user != null ? user.getLastName() : null,
                                    user != null ? user.getEmail() : null,
                                    entry.getStatus()
                            );

                            switch (entry.getStatus()) {
                                case EntrantListEntry.STATUS_WAITLIST:
                                    waitlist.add(display);
                                    break;

                                case EntrantListEntry.STATUS_INVITED:
                                    selected.add(display);
                                    break;

                                case EntrantListEntry.STATUS_REGISTERED:
                                    enrolled.add(display);
                                    break;

                                case EntrantListEntry.STATUS_CANCELLED_OR_REJECTED:
                                    cancelled.add(display);
                                    break;
                            }

                            completedCount[0]++;

                            if (completedCount[0] == totalEntries) {
                                waitlistAdapter.setEntrants(waitlist);
                                selectedAdapter.setEntrants(selected);
                                enrolledAdapter.setEntrants(enrolled);
                                cancelledAdapter.setEntrants(cancelled);
                            }
                        });
                    }
                }).addOnFailureListener(e -> {if (!isAdded()) return;Toast.makeText(requireContext(), "Failed to load entrants", Toast.LENGTH_SHORT).show();});
    }

    private void exportCsv() {
        if (enrolledList == null || enrolledList.isEmpty()) {
            Toast.makeText(requireContext(), "No registered entrants", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: fill
    }
}