package com.example.eventlotterysystem.UI.fragments;

import com.example.eventlotterysystem.model.Event;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateEventFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText nameInput, descInput, timeInput, placeInput, startRegInput, endRegInput, maxInput;

    private Button saveButton;
    private Button backButton;
    @Nullable
    @Override
    /**
     * Allows the user to create a new event by providing details of the event
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view of creating Event
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        db = FirebaseFirestore.getInstance(); // will replaced by the database of Event

        // Edit text
        nameInput = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);
        backButton = view.findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        saveButton.setOnClickListener(v -> {
            // check if all the inputs are valid
            if (checkInfo()){
                // Creaet new Event
                createEvent();
                Toast.makeText(getContext(), "New Event: " + nameInput.getText().toString() + " is created", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getContext(), "Required info is not provided, event create fail", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }


    /**
     * Checking all the information if they are good to upload to the database
     * @return  true if the formats are all correct and non-optional infomation are all filled
     *          false if any infomation is missing or in incorrect format
     */
    private boolean checkInfo() {
        // Checking if all the information is good to upload to the database

        // description has to be filled
        String description = descInput.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "description cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        //  place has to be filled
        String place = placeInput.getText().toString();
        if (place.isEmpty()) {
            Toast.makeText(getContext(), "place cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        //  time has to be filled and in correct format
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

        // Max Entrants must an integer greater than 0 or empty
        String maxEntrants = maxInput.getText().toString();
        if (!maxEntrants.isEmpty()){
            try {
                int val = Integer.parseInt(maxEntrants);
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
     * Base on the users' text
     * make an event and set all the details
     * upload it to the database
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

                    // direct to EventDetailsFragment after it is created
                    EventDetailsFragment fragment = EventDetailsFragment.newInstance(documentReference.getId());
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show()
                );
    }
}