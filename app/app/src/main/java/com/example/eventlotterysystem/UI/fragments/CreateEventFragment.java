package com.example.eventlotterysystem.UI.fragments;

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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        db = FirebaseFirestore.getInstance();

        nameInput = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);
        saveButton.setOnClickListener(v -> {
            if (checkInfo()){
                createEvent();
                Toast.makeText(getContext(), "New Event: " + nameInput.getText().toString() + " is created", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getContext(), "Required info is not provided, event create fail", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private boolean checkInfo() {
        String description = descInput.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "description cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        String place = placeInput.getText().toString();
        if (place.isEmpty()) {
            Toast.makeText(getContext(), "place cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        String time = timeInput.getText().toString();
        if (time.isEmpty()) {
            Toast.makeText(getContext(), "time cannot be Empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        String start = startRegInput.getText().toString();
        String end = endRegInput.getText().toString();
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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
        String maxEntrants = maxInput.getText().toString();

        try {
            Double.parseDouble(maxEntrants);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Registration End is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void createEvent() {
        String name = nameInput.getText().toString();
        String description = descInput.getText().toString();
        String time = timeInput.getText().toString();
        String place = placeInput.getText().toString();
        String start = startRegInput.getText().toString();
        String end = endRegInput.getText().toString();
        Double maxEntrants = null;
        if (!maxInput.getText().toString().isEmpty()) {
            maxEntrants = Double.parseDouble(maxInput.getText().toString());
        }
        Event event = new Event(name, description);
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            event.setEventTime(formatter.parse(time));
        } catch (ParseException e){
            e.printStackTrace();
        }
        event.setEventPlace(place);
        if ((start != null) && (end != null))   {
            try {
                Date date_start = formatter.parse(start);
                Date date_end = formatter.parse(end);
                List<Date> list_reg_date = new ArrayList<>();
                list_reg_date.add(date_start);
                list_reg_date.add(date_end);
                event.setRegistrationDate(list_reg_date);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (maxEntrants != null){
            event.setCapacity(maxEntrants);
        }

        // To do:
        // 1: Save Event to db
        // 2. Create Lists for Event in db (Waiting, Entrants, etc.)
    }
}