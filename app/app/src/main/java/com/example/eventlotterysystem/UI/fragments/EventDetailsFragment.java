package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* ----------- ADDED IMPORTS FOR LOCATION + FIRESTORE ----------- */
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.HashMap;
import java.util.Map;
/* -------------------------------------------------------------- */

/**
 * A simple {@link Fragment} subclass.
 * EventDetailsFragment displays details for a selected event, as well as provides buttons for registering and removing participants.
 * Details such as poster, description, registration period, and waitlist count are displayed (and are collected from firestore database)
 */
public class EventDetailsFragment extends Fragment {

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
    private Button mapButton; // 🔥 ADDED

    private String eventId;
    private String entrantId;

    private int currentStatus = -1;

    private final EntrantListFirebase waitlistDb = new EntrantListFirebase();
    private final EventRepository eventRepository = new EventRepository();

    private ListenerRegistration commentListener;
    private Button seeCommentsButton;

    public EventDetailsFragment() {}

    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        mapButton = view.findViewById(R.id.mapButton); // 🔥 ADDED

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            entrantId = user.getUid();
        }

        if (eventId == null || entrantId == null) {
            registerButton.setEnabled(false);
            valueWaitlistCount.setText("—");
            return;
        }

        registerButton.setOnClickListener(v -> {
            switch (currentStatus) {
                case EntrantListEntry.STATUS_WAITLIST:
                    waitlistDb.updateStatus(eventId, entrantId, EntrantListEntry.STATUS_CANCELLED_OR_REJECTED).addOnSuccessListener(unused -> {
                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();
                        refreshWaitlistCount();
                        Toast.makeText(getContext(), "Removed from waiting list", Toast.LENGTH_SHORT).show();
                    });
                    break;

                case EntrantListEntry.STATUS_INVITED:
                    break;

                case EntrantListEntry.STATUS_REGISTERED:
                    waitlistDb.updateStatus(eventId, entrantId, EntrantListEntry.STATUS_CANCELLED_OR_REJECTED).addOnSuccessListener(unused -> {
                        currentStatus = EntrantListEntry.STATUS_CANCELLED_OR_REJECTED;
                        updateActionButton();
                        Toast.makeText(getContext(), "Registration cancelled", Toast.LENGTH_SHORT).show();
                    });
                    break;

                case EntrantListEntry.STATUS_CANCELLED_OR_REJECTED:
                default:
                    waitlistDb.getEntry(eventId, entrantId).addOnSuccessListener(entry -> {

                        FusedLocationProviderClient fusedLocationClient =
                                LocationServices.getFusedLocationProviderClient(requireContext());

                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {

                            double lat = 0;
                            double lng = 0;

                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }

                            Map<String, Object> data = new HashMap<>();
                            data.put("eventId", eventId);
                            data.put("entrantId", entrantId);
                            data.put("status", EntrantListEntry.STATUS_WAITLIST);
                            data.put("latitude", lat);
                            data.put("longitude", lng);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("events")
                                    .document(eventId)
                                    .collection("waitlist")
                                    .document(entrantId)
                                    .set(data)
                                    .addOnSuccessListener(unused -> {
                                        currentStatus = EntrantListEntry.STATUS_WAITLIST;
                                        updateActionButton();
                                        refreshWaitlistCount();
                                        Toast.makeText(getContext(), "Joined waiting list", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    });
                    break;
            }
        });

        // 🔥 ADDED: Map navigation
        mapButton.setOnClickListener(v -> {
            Fragment fragment = EntrantsMapFragment.newInstance(eventId);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        initializeUI();
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

    private void loadEventDetails() {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                if (!isAdded()) return;

                eventTitle.setText(event.getTitle());
                valueDescription.setText(event.getDescription());
                valueRegistration.setText("Loaded");

                valueStarttime.setText("Not available");
                valueLocation.setText("Not available");
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateActionButton() {
        switch (currentStatus) {
            case EntrantListEntry.STATUS_WAITLIST:
                registerButton.setText("Remove from Waitlist");
                break;
            case EntrantListEntry.STATUS_REGISTERED:
                registerButton.setText("Cancel Registration");
                break;
            default:
                registerButton.setText("Register");
                break;
        }
    }

    private void refreshWaitlistCount() {
        waitlistDb.getWaitlistCount(eventId)
                .addOnSuccessListener(count -> valueWaitlistCount.setText(String.valueOf(count)))
                .addOnFailureListener(e -> valueWaitlistCount.setText("—"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListener != null) {
            commentListener.remove();
        }
    }
}