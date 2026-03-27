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
import com.example.eventlotterysystem.model.EntrantListEntry;

import java.util.ArrayList;
import java.util.List;
import com.example.eventlotterysystem.database.ProfileManager;
/**
 * A simple {@link Fragment} subclass.
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
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();


    public OrganizerEventEntrantsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrganizerEventMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizerEventEntrantsFragment newInstance(String param1, String param2) {
        OrganizerEventEntrantsFragment fragment = new OrganizerEventEntrantsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

        setupRecyclerViews();

        String eventId = requireArguments().getString("eventId");
        loadEntrants(eventId);
        // TODO: button functionalities, notifs and the csv export
    }

    /**
     * This method is used to load the entrants for the event.
     * @param eventId
     *  The id of the event to load the entrants for.
     *  No parameters or returns.
     */
    private void setupRecyclerViews() {
        // I read about the multiple recycler views here, https://www.geeksforgeeks.org/android/how-to-create-a-nested-recyclerview-in-android/
        waitlistAdapter = new EntrantAdapter();
        selectedAdapter = new EntrantAdapter();
        enrolledAdapter = new EntrantAdapter();
        cancelledAdapter = new EntrantAdapter();

        waitlistRecyclerView.setAdapter(waitlistAdapter);
        selectedRecyclerView.setAdapter(selectedAdapter);
        enrolledRecyclerView.setAdapter(enrolledAdapter);
        cancelledRecyclerView.setAdapter(cancelledAdapter);

        // Set up layout managers for each RecyclerView
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        enrolledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cancelledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * This method is used to load the entrants for the event.
     * @param eventId
     *  The id of the event to load the entrants for.
     *  No parameters or returns.
     */
    private void loadEntrants(String eventId){

    }
}