package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * This fragment shows the event details screen from an organizer view
 * Does not allow registration of an event

 * @ Author Jaylin
 */

public class OrganizerEventDetailsFragment extends Fragment{
    // TODO: Add the popup about event info for US 01.05.05
    private static final String EVENT_ID = "event_id";

    // UI elements (buttons, text views, etc.)
    private TextView valueDescription;
    private TextView valueRegistration;
    private TextView valueWaitlistCount;
    private TextView valueStarttime;
    private TextView valueLocation;
    private ImageView eventPoster;
    private ImageButton infoButton;
    private Button backButton;
    private Button addCommentButton;
    private EditText writeCommentBox;
    //Firestore data for these variables
    private Button editEventButton;
    //Firestore data for these variables
    private String eventId; // Unique identifier for the event
    private String entrantId; // Unique identifier for the entrant
    private final EntrantListFirebase waitlistDb = new EntrantListFirebase();

    private final EventRepository eventRepository = new EventRepository();

    public OrganizerEventDetailsFragment() {
        // Required empty public constructor
    }

    /**
     *Create Event Details fragment for specific event with eventId
     * @param eventId
     *  The unique identifier for the event
     * @return fragment
     *  A new instance of EventDetailsFragment
     */
    public static OrganizerEventDetailsFragment newInstance(String eventId) {
        /*
         Author: RobinHood https://stackoverflow.com/users/646806/robinhood
         Title: "How can I transfer data from one fragment to another fragment android"
         Answer: https://stackoverflow.com/a/19333288
         Date: Oct 12, 2013
         */
        OrganizerEventDetailsFragment fragment = new OrganizerEventDetailsFragment();
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
        return inflater.inflate(R.layout.fragment_org_event_detail, container, false);
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
        valueDescription = view.findViewById(R.id.valueDescription);
        valueRegistration = view.findViewById(R.id.valueRegistration);
        valueWaitlistCount = view.findViewById(R.id.valueWaitlistCount);
        valueStarttime = view.findViewById(R.id.valueStartTime);
        valueLocation = view.findViewById(R.id.valueLocation);
        eventPoster = view.findViewById(R.id.eventposter);
        //infoButton = view.findViewById(R.id.infoButton);
        backButton = view.findViewById(R.id.backbutton);
        editEventButton = view.findViewById(R.id.editEventButton);
        addCommentButton = view.findViewById(R.id.add_comment_button);
        writeCommentBox = view.findViewById(R.id.write_comment_box);

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

        editEventButton.setOnClickListener(v -> {
            Fragment fragment = EditEventFragment.newInstance(eventId);
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            // back button ui may be able to be removed? https://developer.android.com/guide/navigation/custom-back
            @Override
            public void onClick(View v) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
//        // add a comment when the add button is pressed
//        addCommentButton.setOnClickListener(v -> {
//            String comment = writeCommentBox.getText().toString();
//            // input validation
//            boolean isValid = true;
//            if (comment.isEmpty()){
//                isValid = false;
//            }
//            // add the comment to firebase
//            if (isValid){
//                UserCommentManager commentManager = UserCommentManager.getInstance();
//                commentManager.addCommentToEvent(eventId, comment, new UserCommentManager.OnCommentAddedListener() {
//                    @Override
//                    public void onSuccess(DocumentReference docRef) {
//
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//
//                    }
//                });
//                // clear the text box
//                writeCommentBox.setText("");
//                // send a comment posted message
//                Toast.makeText(getContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getContext(), "Please write a valid comment", Toast.LENGTH_SHORT).show();
//            }
//
//        });
        /**
        // the lottery system info pop up (future implementation)
        infoButton.setOnClickListener(new View.OnClickListener() {
            // TODO: add the pop up
            @Override
            public void onClick(View v) {
                //TODO
            }
        });*/
        initializeUI(); // button update and get event details
    }


    /**
     * This method is used to initialize the UI elements for the event details fragment
     * based on if the entrant is on the waitlist or not, as the button text will be changed.
     */
    private void initializeUI() {
        refreshWaitlistCount();
        loadEventDetails();
    }

    /**
     * This method is used to load the event details from the database.
     * No parameters or returns.
     */
    private void loadEventDetails() {

        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {

            @Override
            public void onEventLoaded(Event event) {
                if (!isAdded()) return;

                // Description
                valueDescription.setText(event.getDescription());

                // Registration period
                String regPeriod = formatRegistrationPeriod(
                        event.getRegistrationStartDate(),
                        event.getRegistrationEndDate()
                );
                valueRegistration.setText(regPeriod);

                // These fields are not yet in the Event model
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String datetStr = sdf.format(new Date(event.getDate()));
                valueStarttime.setText(datetStr);
                valueLocation.setText(event.getPlace());

                // Waitlist count is handled by refreshWaitlistCount()
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(
                        getContext(),
                        "Failed to load event: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                valueDescription.setText("N/A");
                valueRegistration.setText("Not set");
                valueStarttime.setText("Not available");
                valueLocation.setText("Not available");
            }
        });
    }

    /**
     * This method is used to format the registration period for the event.
     * @param start
     * @param end
     * @return
     */
    private String formatRegistrationPeriod(long start, long end) {
        if (start == 0 || end == 0) {
            return "Not set";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startStr = sdf.format(new Date(start));
        String endStr = sdf.format(new Date(end));

        return startStr + " - " + endStr;
    }

    /**
     * Updates the waitlist count label.
     * No parameters or returns.
     */
    private void refreshWaitlistCount() {
        waitlistDb.getWaitlistCount(eventId)
                .addOnSuccessListener(count -> valueWaitlistCount.setText(String.valueOf(count)))
                .addOnFailureListener(e -> valueWaitlistCount.setText("—"));
    }
}
