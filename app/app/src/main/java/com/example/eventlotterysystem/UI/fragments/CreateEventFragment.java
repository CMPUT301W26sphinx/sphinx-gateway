package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fragment for creating a new event.
 * <p>
 * This fragment provides a form for the user to enter event details such as name,
 * description, date/time, place, registration period, and maximum entrants. After
 * validation, the event is saved to Firestore under the "events" collection.
 * </p>
 *
 * @see EditEventFragment
 */
public class CreateEventFragment extends Fragment {

    /** FirebaseFirestore instance for database operations. */
    private FirebaseFirestore db;

    /** EditText for event name. */
    private EditText nameInput;

    /** EditText for event description. */
    private EditText descInput;

    /** EditText for event time (date and time). */
    private EditText timeInput;

    /** EditText for event place/location. */
    private EditText placeInput;

    /** EditText for registration start date. */
    private EditText startRegInput;

    /** EditText for registration end date. */
    private EditText endRegInput;

    /** EditText for maximum number of entrants. */
    private EditText maxInput;

    /** Button to save the new event. */
    private Button saveButton;

    /**
     * Required empty public constructor.
     */
    public CreateEventFragment() {
    }

    /**
     * Inflates the layout, initializes Firebase Firestore, sets up UI references,
     * and attaches a click listener to the save button.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view that this fragment's UI should be attached to.
     * @param savedInstanceState If non‑null, this fragment is being re‑constructed from a previous saved state.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        db = FirebaseFirestore.getInstance(); // will be replaced by the database of Event

        // Edit text
        nameInput = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);

        saveButton.setOnClickListener(v -> {
            // check if all the inputs are valid
            if (checkInfo()) {
                // Create new Event
                createEvent();
                Toast.makeText(getContext(), "New Event: " + nameInput.getText().toString() + " is created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Required info is not provided, event create fail", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Validates the user input fields.
     * <p>
     * Checks that:
     * <ul>
     *   <li>Description and place are not empty.</li>
     *   <li>Event time is provided and matches the format "dd/MM/yyyy HH:mm".</li>
     *   <li>Registration start and end dates, if provided, are in the correct format and either both present or both absent.</li>
     *   <li>If provided, maximum entrants is a positive integer.</li>
     * </ul>
     * Displays appropriate error messages via {@link Toast} if validation fails.
     * </p>
     *
     * @return {@code true} if all input is valid; {@code false} otherwise.
     */
    private boolean checkInfo() {
        // description has to be filled
        String description = descInput.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "description cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // place has to be filled
        String place = placeInput.getText().toString();
        if (place.isEmpty()) {
            Toast.makeText(getContext(), "place cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // time has to be filled and in correct format
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = timeInput.getText().toString();
        if (time.isEmpty()) {
            Toast.makeText(getContext(), "time cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            formatter.parse(time);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Incorrect date format: Time", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Registration Dates should be both filled or all empty
        String start = startRegInput.getText().toString();
        String end = endRegInput.getText().toString();
        if (!start.isEmpty()) {
            try {
                formatter.parse(start);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Incorrect date format: Registration Start", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!end.isEmpty()) {
            try {
                formatter.parse(end);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Incorrect date format: Registration End", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (start.isEmpty() && !end.isEmpty()) {
            Toast.makeText(getContext(), "Registration Start is missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!start.isEmpty() && end.isEmpty()) {
            Toast.makeText(getContext(), "Registration End is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Max Entrants must be an integer greater than 0 or empty
        String maxEntrants = maxInput.getText().toString();
        if (!maxEntrants.isEmpty()) {
            try {
                double val = Double.parseDouble(maxEntrants);
                if (val <= 0) {
                    Toast.makeText(getContext(), "Max Entrants has to be bigger than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Max Entrants has to be integer", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new event object from the input fields and saves it to Firestore.
     * <p>
     * The event is built using the current {@link Event} model with setters for title,
     * description, capacity, and registration dates. After a successful Firestore write,
     * the document ID is set back into the event object and the document is updated with
     * that ID. A success or failure toast is shown.
     * </p>
     */
    private void createEvent() {
        String name = nameInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String start = startRegInput.getText().toString().trim();
        String end = endRegInput.getText().toString().trim();

        int maxEntrants = 0;
        if (!maxInput.getText().toString().trim().isEmpty()) {
            maxEntrants = Integer.parseInt(maxInput.getText().toString().trim());
        }

        Event event = new Event();
        event.setTitle(name);
        event.setDescription(description);
        event.setCapacity(maxEntrants);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try {
            if (!start.isEmpty()) {
                Date startDate = formatter.parse(start);
                event.setRegistrationStartDate(startDate.getTime());
            }

            if (!end.isEmpty()) {
                Date endDate = formatter.parse(end);
                event.setRegistrationEndDate(endDate.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    event.setEventId(documentReference.getId());
                    documentReference.update("eventId", documentReference.getId());
                    Toast.makeText(getContext(), "New Event: " + name + " is created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show()
                );
    }
}