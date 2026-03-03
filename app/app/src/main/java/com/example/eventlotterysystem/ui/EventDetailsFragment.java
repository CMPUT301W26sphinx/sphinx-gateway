package com.example.eventlotterysystem.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventlotterysystem.R;

/**
 * A simple {@link Fragment} subclass.
 * EventDetailsFragment displays details for a selected event, as well as provides buttons for registering and removing participants.
 * Details such as poster, description, registration period, and waitlist count are displayed (and are collected from firestore database)
 *
 *
 */
public class EventDetailsFragment extends Fragment {

    private static final EVENT_ID = "event_id";

    // UI elements (buttons, text views, etc.)
    private TextView eventTitle;
    private TextView valueDescription;
    private TextView valueRegistration;
    private TextView valueWaitlistCount;
    private ImageView eventPoster;
    private ImageButton infoButton;
    private Button backButton;
    private Button registerButton;

    //Firestore data for these variables
    private String eventId; // Unique identifier for the event
    private boolean isOnlist = False; // Indicates if the user has already pressed register, placeholder


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
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get the xml elements
        eventTitle = view.findViewById(R.id.eventTitle);
        valueDescription = view.findViewById(R.id.valueDescription);
        valueRegistration = view.findViewById(R.id.valueRegistration);
        valueWaitlistCount = view.findViewById(R.id.valueWaitlistCount);
        eventPoster = view.findViewById(R.id.eventposter);
        infoButton = view.findViewById(R.id.infoButton);
        backButton = view.findViewById(R.id.backbutton);
        registerButton = view.findViewById(R.id.registerbutton);

        loadEventData() // get stuff from firestore

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ...
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        // the lottery system info pop up (future implementation)
        infoButton.setOnClickListener(new View.OnClickListener() {
            // TODO: add the pop up
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    /**
     * Updates register button label based on registration state.
     * Update text when pressed (Register/Remove)
     * No parameters or returns.
    */
    private void updateRegisterButton() {
        if (isRegistered) {
            registerButton.setText(R.string.removebutton);
        } else {
            registerButton.setText(R.string.registerbutton);
        }
    }

    private void loadEventData(String eventId){
        // TODO
    }
}