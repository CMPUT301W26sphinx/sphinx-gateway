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

/**
 * Fragment for editing an existing event.
 * <p>
 * This fragment receives event data via {@link Fragment#getArguments()}, populates the UI,
 * and allows the user to update the event's description, capacity, and registration dates.
 * The updated information is saved to Firestore.
 * </p>
 *
 * @see CreateEventFragment
 */
public class EditEventFragment extends Fragment {

    /** FirebaseFirestore instance for database operations. */
    private FirebaseFirestore db;

    /** EditText for event description. */
    private EditText descInput;

    /** EditText for event time (unused in update). */
    private EditText timeInput;

    /** EditText for event place (unused in update). */
    private EditText placeInput;

    /** EditText for registration start date. */
    private EditText startRegInput;

    /** EditText for registration end date. */
    private EditText endRegInput;

    /** EditText for maximum number of entrants. */
    private EditText maxInput;

    /** TextView for event name (read‑only). */
    private TextView name;

    /** Button to save changes. */
    private Button saveButton;

    /** Firestore document ID of the event being edited. */
    private String eventId;

    /** Date formatter used for parsing and displaying registration dates. */
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.CANADA);

    /**
     * Required empty public constructor.
     */
    public EditEventFragment() {
    }

    /**
     * Inflates the layout, initializes Firebase Firestore, sets up UI references,
     * loads any passed arguments, and sets the click listener for the save button.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view that this fragment's UI should be attached to.
     * @param savedInstanceState If non‑null, this fragment is being re‑constructed from a previous saved state.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        db = FirebaseFirestore.getInstance();

        name = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);

        loadArguments();

        saveButton.setOnClickListener(v -> {
            if (checkInfo()) {
                updateEvent();
            }
        });

        return view;
    }

    /**
     * Retrieves event data from the fragment's arguments and populates the input fields.
     * <p>
     * The following keys are expected in the arguments bundle:
     * "eventId", "title", "description", "registrationStartDate", "registrationEndDate", "capacity".
     * If a key is missing or its value is empty/default, the corresponding field is left blank or with a placeholder.
     * </p>
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
     * Validates the user input fields.
     * <p>
     * Checks that:
     * <ul>
     *   <li>Description is not empty.</li>
     *   <li>If provided, registration start and end dates are in the correct format ("dd/MM/yyyy HH:mm").</li>
     *   <li>Both registration dates are either both provided or both empty.</li>
     *   <li>If provided, maximum entrants is a positive integer.</li>
     * </ul>
     * Displays appropriate error messages via {@link Toast} if validation fails.
     * </p>
     *
     * @return {@code true} if all input is valid; {@code false} otherwise.
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
     * Updates the event document in Firestore with the current input values.
     * <p>
     * This method updates the fields "description", "capacity", "registrationStartDate",
     * and "registrationEndDate" for the event document identified by {@link #eventId}.
     * If a date field is empty, it is set to 0 (epoch) in Firestore.
     * Shows success or failure messages via {@link Toast}.
     * </p>
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
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update event", Toast.LENGTH_SHORT).show()
                );
    }
}