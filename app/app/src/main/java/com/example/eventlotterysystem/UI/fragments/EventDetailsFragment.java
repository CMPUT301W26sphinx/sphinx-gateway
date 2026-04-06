package com.example.eventlotterysystem.UI.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.CommentAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.UserComment;
import com.example.eventlotterysystem.utils.ImageHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * EventDetailsFragment displays details for a selected event, as well as provides buttons for registering and removing participants.
 * Details such as poster, description, registration period, and waitlist count are displayed (and are collected from firestore database)
 */
public class EventDetailsFragment extends Fragment {
    // TODO: Array adapter for other fragment logic US 01.01.03
    // TODO: add event details load in once event firebase and class is set up
    // TODO: Add the popup about event info for US 01.05.05
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
    private Button addCommentButton;
    private EditText writeCommentBox;
    //Firestore data for these variables
    private String eventId; // Unique identifier for the event
    private String entrantId; // Unique identifier for the entrant
    // for button switch logic
    private int currentStatus = -1; // Affect UI button

    private final EntrantListFirebase waitlistDb = new EntrantListFirebase();
    private final EventRepository eventRepository = new EventRepository();

    private CommentAdapter commentAdapter;
    private List<UserComment> commentList;
    private UserCommentManager commentManager = UserCommentManager.getInstance();

    private ListenerRegistration commentListener;

    private Button seeCommentsButton;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Create Event Details fragment for specific event with eventId
     *
     * @param eventId The unique identifier for the event
     * @return fragment
     * A new instance of EventDetailsFragment
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
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get the xml elements
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
        seeCommentsButton = view.findViewById(R.id.seeCommentsButton);


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

        if (eventId == null || entrantId == null) {
            registerButton.setEnabled(false);
            valueWaitlistCount.setText("—");
            return;
        }

        // TODO: consider how to remove or change button when registration period closed
        registerButton.setOnClickListener(v -> {
            switch (currentStatus) {
                case EntrantListEntry.STATUS_WAITLIST: //if on waitlist, remove from waitlist button
                    waitlistDb.updateStatus(eventId, entrantId, EntrantListEntry.STATUS_CANCELLED_OR_REJECTED).addOnSuccessListener(unused -> {
                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();
                        refreshWaitlistCount();
                        Toast.makeText(getContext(), "Removed from waiting list", Toast.LENGTH_SHORT).show();
                    });
                    break;

                case EntrantListEntry.STATUS_INVITED:
                    // TODO: Add logic for moving to inivation screen to respond
                    break;

                case EntrantListEntry.STATUS_REGISTERED: //if registered, cancel registration button
                    waitlistDb.updateStatus(eventId, entrantId, EntrantListEntry.STATUS_CANCELLED_OR_REJECTED).addOnSuccessListener(unused -> {
                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();
                        Toast.makeText(getContext(), "Registration cancelled", Toast.LENGTH_SHORT).show();
                    });
                    break;

                case EntrantListEntry.STATUS_CANCELLED_OR_REJECTED: //if cancelled or rejected, register button
                default: // if not on list or cancelled, option to register
                    registerWithCurrentLocation();
                    break;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            // back button ui may be able to be removed? https://developer.android.com/guide/navigation/custom-back
            @Override
            public void onClick(View v) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        seeCommentsButton.setOnClickListener(v -> {
            Fragment fragment = new EventComments();

            Bundle bundle = new Bundle();
            bundle.putString("event_id", eventId);
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // the lottery system info pop up (future implementation)
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.activity_terms, null);

                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                Button understandBtn = dialogView.findViewById(R.id.btn_understand);
                understandBtn.setOnClickListener(view -> dialog.dismiss());

                dialog.show();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
            }
        });
        initializeUI(); // button update and get event details


    }


    /**
     * This method is used to initialize the UI elements for the event details fragment
     * based on if the entrant is on the waitlist or not, as the button text will be changed.
     */

    private void registerWithCurrentLocation() {
        if (!isAdded()) return;

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission is required to register", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        completeRegistrationWithLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(lastLocation -> {
                            if (lastLocation != null) {
                                completeRegistrationWithLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                            } else {
                                Toast.makeText(getContext(), "Could not get your location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Could not get your location", Toast.LENGTH_SHORT).show());
    }

    private void completeRegistrationWithLocation(double lat, double lng) {
        if (entrantId == null || eventId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        java.util.Map<String, Object> userLocation = new java.util.HashMap<>();
        userLocation.put("latitude", lat);
        userLocation.put("longitude", lng);

        db.collection("users")
                .document(entrantId)
                .set(userLocation, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        waitlistDb.getEntry(eventId, entrantId).addOnSuccessListener(entry -> {
                            if (entry == null) {
                                EntrantListEntry newEntry = new EntrantListEntry(
                                        eventId,
                                        entrantId,
                                        EntrantListEntry.STATUS_WAITLIST,
                                        lat,
                                        lng
                                );
                                waitlistDb.upsertEntry(eventId, newEntry).addOnSuccessListener(innerUnused -> {
                                    currentStatus = EntrantListEntry.STATUS_WAITLIST;
                                    updateActionButton();
                                    refreshWaitlistCount();
                                    Toast.makeText(getContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                entry.setStatus(EntrantListEntry.STATUS_WAITLIST);
                                entry.setLatitude(lat);
                                entry.setLongitude(lng);
                                waitlistDb.upsertEntry(eventId, entry).addOnSuccessListener(innerUnused -> {
                                    currentStatus = EntrantListEntry.STATUS_WAITLIST;
                                    updateActionButton();
                                    refreshWaitlistCount();
                                    Toast.makeText(getContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }));
    }

    private void initializeUI() {
        waitlistDb.getEntrantStatus(eventId, entrantId).addOnSuccessListener(status -> {
            currentStatus = status;
            updateActionButton();
        }).addOnFailureListener(e -> {
            currentStatus = -1;
            updateActionButton();
        });

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

                // Title
                eventTitle.setText(event.getTitle());

                // Description
                valueDescription.setText(event.getDescription());

                // Registration period
                String regPeriod = formatRegistrationPeriod(event.getRegistrationStartDate(), event.getRegistrationEndDate());
                valueRegistration.setText(regPeriod);

                // These fields are not yet in the Event model
                valueStarttime.setText("Not available");
                valueLocation.setText("Not available");

                // Waitlist count is handled by refreshWaitlistCount()

                // ADDED: load event poster if available, otherwise keep placeholder
                if (event.getImageData() != null && !event.getImageData().isEmpty()) {
                    ImageHelper.loadEventImage(eventPoster, event);
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                eventTitle.setText("Unknown Event");
                valueDescription.setText("N/A");
                valueRegistration.setText("Not set");
                valueStarttime.setText("Not available");
                valueLocation.setText("Not available");
            }
        });
    }

    /**
     * This method is used to format the registration period for the event.
     *
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
     * Updates register button label based on registration state.
     * Update text when pressed (Register/Remove)
     * No parameters or returns.
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

            case EntrantListEntry.STATUS_CANCELLED_OR_REJECTED:
            default:
                registerButton.setText("Register");
                break;
        }
    }

    /**
     * Updates the waitlist count label.
     * No parameters or returns.
     */
    private void refreshWaitlistCount() {
        waitlistDb.getWaitlistCount(eventId).addOnSuccessListener(count -> valueWaitlistCount.setText(String.valueOf(count))).addOnFailureListener(e -> valueWaitlistCount.setText("—"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListener != null) {
            commentListener.remove();
        }
    }


}