package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditEventFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText descInput, timeInput, placeInput, startRegInput, endRegInput, maxInput;
    private TextView name;
    private Button saveButton;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        db = FirebaseFirestore.getInstance();

        name = view.findViewById(R.id.eventName);
//        name.setText(db.get(Name));
        descInput = view.findViewById(R.id.eventDescription);
//        descInput.setText(db.get(description));
        timeInput = view.findViewById(R.id.eventTime);
//        timeInput.setText(db.get(time));
        placeInput = view.findViewById(R.id.eventPlace);
//        placeInput.setText(db.get(place));
        startRegInput = view.findViewById(R.id.regStart);
//        startRegInput.setText(db.get(startReg));
        endRegInput = view.findViewById(R.id.regEnd);
//        endRegInput.setText(db.get(endReg));
        maxInput = view.findViewById(R.id.maxEntrants);
//        maxInput.setText(db.get(max));
        saveButton = view.findViewById(R.id.saveEventButton);
        saveButton.setOnClickListener(v -> {
            if (checkInfo()){
                updateEvent();
                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
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
    private void updateEvent(){
        //to do
        // db.set
        // Event event = db.get();
        Event event = new Event("Test name", "Test desc"); // delete after all coding
        String description = descInput.getText().toString();
        String place = placeInput.getText().toString();
        String time = timeInput.getText().toString();
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String start = startRegInput.getText().toString();
        String end = endRegInput.getText().toString();
        Double maxEntrants = Double.POSITIVE_INFINITY;
        if (!maxInput.getText().toString().isEmpty()) {
            maxEntrants = Double.parseDouble(maxInput.getText().toString());
        }
        event.setEventDescription(description);
        event.setEventPlace(place);
        try {
            event.setEventTime(formatter.parse(time));
        } catch (ParseException e){
            e.printStackTrace();
        }
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
        event.setCapacity(maxEntrants);
    }
}