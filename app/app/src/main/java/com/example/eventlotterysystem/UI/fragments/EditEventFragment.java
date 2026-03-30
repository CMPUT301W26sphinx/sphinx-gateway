package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditEventFragment extends Fragment {

    private FirebaseFirestore db;

    private EditText descInput;
    private EditText timeInput;
    private EditText placeInput;
    private EditText startRegInput;
    private EditText endRegInput;
    private EditText maxInput;
    private TextView name;
    private Button saveButton;
    private Button backButton;

    private String eventId;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.CANADA);

    private static final String EVENT_ID = "event_id";


    public EditEventFragment() {}

    public static EditEventFragment newInstance(String eventId) {
        EditEventFragment fragment = new EditEventFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    /**
     * Allows the user to edit an existed event
     * New info will replace the old info in database if all the new info are valid
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view of editing Event
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        db = FirebaseFirestore.getInstance();

        name = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);
        backButton = view.findViewById(R.id.backButton);


        loadArguments();
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        saveButton.setOnClickListener(v -> {
            if (checkInfo()) {
                updateEvent();
            }
        });

        return view;
    }

    /**
     * Loading the event details from the database
     * (The old info)
     */
    private void loadArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        eventId = args.getString("eventId", "");

        String title = args.getString("title", "");
        String description = args.getString("description", "");
        long registrationStart = args.getLong("registrationStartDate", 0L);
        long registrationEnd = args.getLong("registrationEndDate", 0L);
        int capacity = args.getInt("capacity", 0);

        name.setText(title);
        descInput.setText(description);

        if (registrationStart > 0) {
            startRegInput.setText(formatter.format(new Date(registrationStart)));
        }

        if (registrationEnd > 0) {
            endRegInput.setText(formatter.format(new Date(registrationEnd)));
        }

        if (capacity > 0) {
            maxInput.setText(String.valueOf(capacity));
        }
    }

    /**
     * Checking all the info if they are good to upload to the database
     * (new info)
     * @return  true if the formats are all correct and non-optional info are all filled
     *          false if any info is missing or in incorrect format
     */
    private boolean checkInfo() {
        String description = descInput.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        String start = startRegInput.getText().toString().trim();
        String end = endRegInput.getText().toString().trim();

        if (!TextUtils.isEmpty(start)) {
            try {
                formatter.parse(start);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Incorrect date format: Registration Start", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (!TextUtils.isEmpty(end)) {
            try {
                formatter.parse(end);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Incorrect date format: Registration End", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            Toast.makeText(getContext(), "Registration Start is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) {
            Toast.makeText(getContext(), "Registration End is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        String maxEntrants = maxInput.getText().toString().trim();
        if (!maxEntrants.isEmpty()) {
            try {
                int val = Integer.parseInt(maxEntrants);
                if (val <= 0) {
                    Toast.makeText(getContext(), "Max Entrants has to be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Max Entrants must be an integer", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    /**
     * update the database with new info
     */
    private void updateEvent() {
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(getContext(), "Event ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = descInput.getText().toString().trim();
        String start = startRegInput.getText().toString().trim();
        String end = endRegInput.getText().toString().trim();

        int capacity = 0;
        String maxEntrantsText = maxInput.getText().toString().trim();
        if (!maxEntrantsText.isEmpty()) {
            capacity = Integer.parseInt(maxEntrantsText);
        }

        Long registrationStartMillis = null;
        Long registrationEndMillis = null;

        try {
            if (!start.isEmpty()) {
                Date startDate = formatter.parse(start);
                if (startDate != null) {
                    registrationStartMillis = startDate.getTime();
                }
            }

            if (!end.isEmpty()) {
                Date endDate = formatter.parse(end);
                if (endDate != null) {
                    registrationEndMillis = endDate.getTime();
                }
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid registration date format", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .update(
                        "description", description,
                        "capacity", capacity,
                        "registrationStartDate", registrationStartMillis == null ? 0L : registrationStartMillis,
                        "registrationEndDate", registrationEndMillis == null ? 0L : registrationEndMillis
                )
                .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                            // direct back to EventDetailsFragment after updating
                            EventDetailsFragment fragment = EventDetailsFragment.newInstance(eventId);
                            getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .commit();
                        }
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update event", Toast.LENGTH_SHORT).show()
                );
    }
}
