package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

/**
 * Fragment responsible for displaying detailed information about a selected event.
 *
 * <p>This fragment retrieves event information from Firebase Firestore and displays
 * details such as:
 * <ul>
 *     <li>Event title</li>
 *     <li>Event description</li>
 *     <li>Registration period</li>
 *     <li>Waitlist count</li>
 * </ul>
 *
 * <p>The fragment also allows a user to:
 * <ul>
 *     <li>Join the waiting list</li>
 *     <li>Cancel registration</li>
 *     <li>Remove themselves from the waiting list</li>
 * </ul>
 *
 * <p>Data interactions are handled through Firebase Authentication and Firestore.
 */
public class EventDetailsFragment extends Fragment {

    /** Key used to pass the event ID through a bundle */
    private static final String EVENT_ID = "event_id";

    // UI elements (buttons, text views, etc.)
    private TextView eventTitle;
    private TextView valueDescription;
    private TextView valueRegistration;
    private TextView valueWaitlistCount;
    private TextView valueStarttime;
    private TextView valueLocation;
    private ImageView eventPoster;
    private ImageButton infoButton;
    private Button backButton;
    private Button registerButton;

    /** Unique identifier for the event */
    private String eventId;

    /** Unique identifier for the current entrant */
    private String entrantId;

    /** Tracks the current registration state of the user */
    private int currentStatus = -1;

    /** Firebase helper class for managing entrant lists */
    private final EntrantListFirebase waitlistDb = new EntrantListFirebase();

    /**
     * Required empty constructor for fragment initialization.
     */
    public EventDetailsFragment() {
    }

    /**
     * Creates a new instance of EventDetailsFragment.
     *
     * @param eventId The ID of the event to display.
     * @return A configured EventDetailsFragment.
     */
    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater used to inflate the layout
     * @param container Parent view
     * @param savedInstanceState Saved state
     * @return Inflated fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Called after the fragment view has been created.
     * Used to initialize UI elements and event listeners.
     *
     * @param view Fragment view
     * @param savedInstanceState Saved state
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        eventTitle = view.findViewById(R.id.eventTitle);
        valueDescription = view.findViewById(R.id.valueDescription);
        valueRegistration = view.findViewById(R.id.valueRegistration);
        valueWaitlistCount = view.findViewById(R.id.valueWaitlistCount);
        valueStarttime = view.findViewById(R.id.valueStartTime);
        valueLocation = view.findViewById(R.id.valueLocation);
        eventPoster = view.findViewById(R.id.eventposter);
        infoButton = view.findViewById(R.id.infoButton);
        backButton = view.findViewById(R.id.backbutton);
        registerButton = view.findViewById(R.id.registerbutton);

        // Retrieve event ID from fragment arguments
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }

        // Get currently logged-in Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            entrantId = user.getUid();
        }

        // Disable button if required IDs are missing
        if (eventId == null || entrantId == null) {
            registerButton.setEnabled(false);
            valueWaitlistCount.setText("—");
            return;
        }

        /**
         * Handles register button behavior depending on the
         * user's current registration status.
         */
        registerButton.setOnClickListener(v -> {

            switch (currentStatus) {

                case EntrantListEntry.STATUS_WAITLIST:

                    // Remove user from waitlist
                    waitlistDb.updateStatus(
                            eventId,
                            entrantId,
                            EntrantListEntry.STATUS_CANCELLED_OR_REJECTED
                    ).addOnSuccessListener(unused -> {

                        // Decrease waitlist count
                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventId)
                                .update("waitingListCount", FieldValue.increment(-1));

                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();

                        Toast.makeText(getContext(), "Removed from waiting list", Toast.LENGTH_SHORT).show();
                    });

                    break;

                case EntrantListEntry.STATUS_INVITED:
                    // Invitation response logic can be implemented here
                    break;

                case EntrantListEntry.STATUS_REGISTERED:

                    // Cancel existing registration
                    waitlistDb.updateStatus(
                            eventId,
                            entrantId,
                            EntrantListEntry.STATUS_CANCELLED_OR_REJECTED
                    ).addOnSuccessListener(unused -> {

                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventId)
                                .update("waitingListCount", FieldValue.increment(-1));

                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();

                        Toast.makeText(getContext(), "Registration cancelled", Toast.LENGTH_SHORT).show();
                    });

                    break;

                default:

                    // Add user to waiting list
                    waitlistDb.getEntry(eventId, entrantId)
                            .addOnSuccessListener(entry -> {

                                if(entry == null){

                                    EntrantListEntry newEntry = new EntrantListEntry(
                                            eventId,
                                            entrantId,
                                            EntrantListEntry.STATUS_WAITLIST
                                    );

                                    waitlistDb.upsertEntry(eventId, newEntry)
                                            .addOnSuccessListener(unused -> {

                                                FirebaseFirestore.getInstance()
                                                        .collection("events")
                                                        .document(eventId)
                                                        .update("waitingListCount", FieldValue.increment(1));

                                                currentStatus = EntrantListEntry.STATUS_WAITLIST;
                                                updateActionButton();

                                                Toast.makeText(
                                                        getContext(),
                                                        "Joined waiting list",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            });

                                } else{

                                    waitlistDb.updateStatus(
                                            eventId,
                                            entrantId,
                                            EntrantListEntry.STATUS_WAITLIST
                                    ).addOnSuccessListener(unused -> {

                                        FirebaseFirestore.getInstance()
                                                .collection("events")
                                                .document(eventId)
                                                .update("waitingListCount", FieldValue.increment(1));

                                        currentStatus = EntrantListEntry.STATUS_WAITLIST;
                                        updateActionButton();

                                        Toast.makeText(
                                                getContext(),
                                                "Joined waiting list",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                                }
                            });

                    break;
            }
        });

        // Back button navigation
        backButton.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        initializeUI();
    }

    /**
     * Initializes UI state by retrieving the entrant's current
     * registration status and loading event data.
     */
    private void initializeUI() {

        waitlistDb.getEntrantStatus(eventId, entrantId)
                .addOnSuccessListener(status -> {
                    currentStatus = status;
                    updateActionButton();
                })
                .addOnFailureListener(e -> {
                    currentStatus = -1;
                    updateActionButton();
                });

        listenToWaitlistCount();
        loadEventDetails();
    }

    /**
     * Retrieves event information from Firestore
     * and populates the UI elements.
     */
    private void loadEventDetails() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String title = document.getString("title");
                        String description = document.getString("description");

                        if (title != null)
                            eventTitle.setText(title);

                        if (description != null)
                            valueDescription.setText(description);
                    }
                });
    }

    /**
     * Attaches a real-time Firestore listener to update
     * the waitlist count whenever it changes.
     */
    private void listenToWaitlistCount() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .addSnapshotListener((document, error) -> {

                    if (error != null) return;

                    if (document != null && document.exists()) {

                        Long count = document.getLong("waitingListCount");

                        if (count != null)
                            valueWaitlistCount.setText(String.valueOf(count));
                    }
                });
    }

    /**
     * Updates the register button label depending on
     * the entrant's current status.
     */
    private void updateActionButton() {

        switch (currentStatus) {

            case EntrantListEntry.STATUS_WAITLIST:
                registerButton.setText("Remove from Waitlist");
                break;

            case EntrantListEntry.STATUS_INVITED:
                registerButton.setText("Respond to Invitation");
                break;

            case EntrantListEntry.STATUS_REGISTERED:
                registerButton.setText("Cancel Registration");
                break;

            default:
                registerButton.setText("Register");
                break;
        }
    }
}