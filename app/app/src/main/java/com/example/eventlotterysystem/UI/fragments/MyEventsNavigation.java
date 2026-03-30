package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.eventlotterysystem.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyEventsNavigation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyEventsNavigation extends Fragment {


    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;

    private TextView eventTitle;
    private ImageButton infoButton;
    private TextView tabWaitlist;
    private TextView tabInvites;
    private TextView tabRegistered;
    private TextView tabHistory;
    private View indicatorWaitlist;
    private View indicatorInvites;
    private View indicatorRegistered;
    private View indicatorHistory;

    public MyEventsNavigation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId The unique identifier for the event.
     * @return A new instance of fragment OrganizerEventNavigationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyEventsNavigation newInstance(String eventId) {
        MyEventsNavigation fragment = new MyEventsNavigation();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_events_navigation, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventTitle = view.findViewById(R.id.eventTitle);
        infoButton = view.findViewById(R.id.infoButton);
        tabWaitlist = view.findViewById(R.id.tabWaitlist);
        tabInvites = view.findViewById(R.id.tabInvites);
        tabRegistered = view.findViewById(R.id.tabRegistered);
        tabHistory = view.findViewById(R.id.tabHistory);
        indicatorWaitlist = view.findViewById(R.id.indicatorWaitlist);
        indicatorInvites = view.findViewById(R.id.indicatorInvites);
        indicatorRegistered = view.findViewById(R.id.indicatorRegistered);
        indicatorHistory = view.findViewById(R.id.indicatorHistory);


        if (infoButton != null) {
            infoButton.setOnClickListener(v -> {
                // placeholder
            });
        }

        tabWaitlist.setOnClickListener(v -> showWaitlistTab());
        tabInvites.setOnClickListener(v -> showInvitesTab());
        //tabRegistered.setOnClickListener(v -> showRegisteredTab());
        //tabHistory.setOnClickListener(v -> showHistoryTab());


        showWaitlistTab();
    }

    private void showWaitlistTab() {
        indicatorWaitlist.setVisibility(View.VISIBLE);
        indicatorInvites.setVisibility(View.INVISIBLE);
        indicatorRegistered.setVisibility(View.INVISIBLE);
        indicatorHistory.setVisibility(View.INVISIBLE);

        ViewWaitListFragment fragment = ViewWaitListFragment.newInstance(eventId);
        getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
    private void showInvitesTab() {
        indicatorWaitlist.setVisibility(View.INVISIBLE);
        indicatorInvites.setVisibility(View.VISIBLE);
        indicatorRegistered.setVisibility(View.INVISIBLE);
        indicatorHistory.setVisibility(View.INVISIBLE);

        AcceptEventInviteFragment fragment = new AcceptEventInviteFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
    private void showRegisteredTab(){
        indicatorWaitlist.setVisibility(View.INVISIBLE);
        indicatorInvites.setVisibility(View.INVISIBLE);
        indicatorRegistered.setVisibility(View.VISIBLE);
        indicatorHistory.setVisibility(View.INVISIBLE);

        // UPDATE: OrganizerEventMapFragment fragment = OrganizerEventMapFragment.newInstance(eventId);
        //getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
    private void showHistoryTab(){
        indicatorWaitlist.setVisibility(View.INVISIBLE);
        indicatorInvites.setVisibility(View.INVISIBLE);
        indicatorRegistered.setVisibility(View.INVISIBLE);
        indicatorHistory.setVisibility(View.VISIBLE);

        // UPDATE: OrganizerEventMapFragment fragment = OrganizerEventMapFragment.newInstance(eventId);
        //getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
}