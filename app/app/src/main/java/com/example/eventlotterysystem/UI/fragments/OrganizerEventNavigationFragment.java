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
 * THis class allows navigation for an organizer to view details about thier event
 * Uses child fragments: OrganizerEventDetailsFragment, OrganizerEventEntrantsFragment, OrganizerEventMapFragment
 * Use the {@link OrganizerEventNavigationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerEventNavigationFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;

    private TextView eventTitle;
    private ImageButton infoButton;
    private TextView tabDetails;
    private TextView tabEntrants;
    private TextView tabMap;
    private View indicatorDetails;
    private View indicatorEntrants;
    private View indicatorMap;

    public OrganizerEventNavigationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId The unique identifier for the event.
     * @return A new instance of fragment OrganizerEventNavigationFragment.
     */
    public static OrganizerEventNavigationFragment newInstance(String eventId) {
        OrganizerEventNavigationFragment fragment = new OrganizerEventNavigationFragment();
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
        return inflater.inflate(R.layout.fragment_organizer_event_navigation, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventTitle = view.findViewById(R.id.eventTitle);
        infoButton = view.findViewById(R.id.infoButton);
        tabDetails = view.findViewById(R.id.tabDetails);
        tabEntrants = view.findViewById(R.id.tabEntrants);
        tabMap = view.findViewById(R.id.tabMap);
        indicatorDetails = view.findViewById(R.id.indicatorDetails);
        indicatorEntrants = view.findViewById(R.id.indicatorEntrants);
        indicatorMap = view.findViewById(R.id.indicatorMap);
        if (infoButton != null) {
            infoButton.setOnClickListener(v -> {
                // placeholder
            });
        }

        tabDetails.setOnClickListener(v -> showDetailsTab());
        tabEntrants.setOnClickListener(v -> showEntrantsTab());
        tabMap.setOnClickListener(v -> showMapsTab());
        showDetailsTab();
    }

    private void showEntrantsTab() {
        indicatorDetails.setVisibility(View.INVISIBLE);
        indicatorEntrants.setVisibility(View.VISIBLE);
        indicatorMap.setVisibility(View.INVISIBLE);

        OrganizerEventEntrantsFragment fragment = OrganizerEventEntrantsFragment.newInstance(eventId);
        getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
    private void showDetailsTab() {
        indicatorDetails.setVisibility(View.VISIBLE);
        indicatorEntrants.setVisibility(View.INVISIBLE);
        indicatorMap.setVisibility(View.INVISIBLE);

        OrganizerEventDetailsFragment fragment = OrganizerEventDetailsFragment.newInstance(eventId);
        getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
    private void showMapsTab(){
        indicatorDetails.setVisibility(View.INVISIBLE);
        indicatorEntrants.setVisibility(View.INVISIBLE);
        indicatorMap.setVisibility(View.VISIBLE);

        OrganizerEventMapFragment fragment = OrganizerEventMapFragment.newInstance(eventId);
        getChildFragmentManager().beginTransaction().replace(R.id.eventInfoChildContainer, fragment).commit();
    }
}