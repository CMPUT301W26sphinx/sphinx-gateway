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
import com.example.eventlotterysystem.database.WaitlistFirebase;
import com.example.eventlotterysystem.model.WaitlistEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/**
 * A simple {@link Fragment} subclass.
 * EventDetailsFragment displays details for a selected event, as well as provides buttons for registering and removing participants.
 * Details such as poster, description, registration period, and waitlist count are displayed (and are collected from firestore database)
 */
public class EventDetailsFragment extends Fragment {
    // TODO: add count logic US 01.05.04
    // TODO: Array adapter for other fragment logic US 01.01.03
    // TODO: Add the popup about event info for US 01.05.05
    private static final String EVENT_ID = "event_id";

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
    private String entrantId; // Unique identifier for the entrant
    // for button switch logic
    private boolean isOnlist; // True if the entrant is on the waitlist, false otherwise

    private final WaitlistFirebase waitlistDb = new WaitlistFirebase();

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    /**
     *Create Event Details fragment for specific event with eventId
     * @param eventId
     *  The unique identifier for the event
     * @return fragment
     *  A new instance of EventDetailsFragment
     */
    public static EventDetailsFragment newInstance(String eventId) {
        /*
         Author: RobinHood https://stackoverflow.com/users/646806/robinhood
         Title: "How can I transfer data from one fragment to another fragment android"
         Answer: https://stackoverflow.com/a/19333288
         Date: Oct 12, 2013
         */
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Default from fragment creation.
     * @param inflater
     *  The LayoutInflater object that can be used to inflate
     *  any views in the fragment,
     * @param container
     *  If non-null, this is the parent view that the fragment's
     *  UI should be attached to.  The fragment should not add the view itself,
     *  but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState
     *  If non-null, this fragment is being re-constructed
     *  from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.
     * @param view
     *  The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState
     *  If non-null, this fragment is being re-constructed
     *  from a previous saved state as given here.
     */
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

        // get the id
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }
        // based on current main logic in branch feature/01.07.01-user-authentication
        // anon auth https://firebase.google.com/docs/auth/android/anonymous-auth?_gl=1*5z5vr9*_up*MQ..*_ga*MTk5ODcwOTI2Mi4xNzcyNDg3MDgy*_ga_CW55HF8NVT*czE3NzI0ODcwODIkbzEkZzAkdDE3NzI0ODcwODIkajYwJGwwJGgw#java
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            entrantId = user.getUid();
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnlist) {
                    WaitlistEntry entry = new WaitlistEntry(eventId, entrantId);
                    waitlistDb.updateWaitlist(eventId, entry).addOnSuccessListener(unused -> {
                        isOnlist = true;
                        updateRegisterButton();
                    });
                } else {
                    waitlistDb.removeWaitlistEntry(eventId, entrantId).addOnSuccessListener(unused -> {
                        isOnlist = false;
                        updateRegisterButton();
                    });
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            // back button ui may be able to be removed? https://developer.android.com/guide/navigation/custom-back
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
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
        initializeUI();
    }

        /**
         * This method is used to initialize the UI elements for the event details fragment
         * based on if the entrant is on the waitlist or not, as the button text will be changed.
         */
        private void initializeUI() {
            waitlistDb.isEntrantInWaitlist(eventId, entrantId).addOnSuccessListener(result -> {
                isOnlist = result;
                updateRegisterButton();
            })
            .addOnFailureListener(e -> {
                isOnlist = false;
                updateRegisterButton();
            });
        }

    /**
     * Updates register button label based on registration state.
     * Update text when pressed (Register/Remove)
     * No parameters or returns.
    */
    private void updateRegisterButton() {
        registerButton.setText(isOnlist ? "Remove" : "Register");
    }
}