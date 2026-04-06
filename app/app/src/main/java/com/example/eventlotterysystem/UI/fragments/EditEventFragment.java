package com.example.eventlotterysystem.UI.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.utils.ImageHelper;
import com.example.eventlotterysystem.utils.ImageUploadHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This fragment allows the user to edit an existing event.
 */
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
    private Switch privacy_Switch;
    private final EventRepository eventRepository = new EventRepository();

    // ADDED: image upload fields
    private ImageView previewImage;
    private ImageUploadHelper imageUploadHelper;
    private String uploadedImageBase64 = null;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the launcher in onCreate, before the fragment is STARTED
        imageUploadHelper = new ImageUploadHelper(this);
    }

    @Override
    /**
     * Allows the user to view the current waiting list
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view of waiting list
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_event, container, false);
    }

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }

        db = FirebaseFirestore.getInstance();
        privacy_Switch = view.findViewById(R.id.privacySwitch);
        name = view.findViewById(R.id.eventName);
        descInput = view.findViewById(R.id.eventDescription);
        timeInput = view.findViewById(R.id.eventTime);
        placeInput = view.findViewById(R.id.eventPlace);
        startRegInput = view.findViewById(R.id.regStart);
        endRegInput = view.findViewById(R.id.regEnd);
        maxInput = view.findViewById(R.id.maxEntrants);
        saveButton = view.findViewById(R.id.saveEventButton);
        backButton = view.findViewById(R.id.backButton);

        // ADDED: find image upload UI elements
        Button uploadImageButton = view.findViewById(R.id.uploadImageButton);
        previewImage = view.findViewById(R.id.previewImage);

        timeInput.setFocusable(false);
        startRegInput.setFocusable(false);
        endRegInput.setFocusable(false);

        // Loading info
        loadEventDetails();


        privacy_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                privacy_Switch.setText("Private");
            } else {
                privacy_Switch.setText("Public");
            }
        });
        timeInput.setOnClickListener(v ->
                showDateTimePicker(timeInput)
        );
        startRegInput.setOnClickListener(v ->
                showDateTimePicker(startRegInput)
        );
        endRegInput.setOnClickListener(v ->
                showDateTimePicker(endRegInput)
        );

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ADDED: image upload button listener
        uploadImageButton.setOnClickListener(v -> {
            imageUploadHelper.pickImage(new ImageUploadHelper.ImageUploadCallback() {
                @Override
                public void onImageLoaded(String base64Image) {
                    uploadedImageBase64 = base64Image;
                    // Preview the newly picked image using a dummy event
                    Event dummyEvent = new Event();
                    dummyEvent.setEventId("preview");
                    dummyEvent.setImageData(base64Image);
                    ImageHelper.loadEventImage(previewImage, dummyEvent);
                    previewImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        saveButton.setOnClickListener(v -> {
            if (checkInfo()) {
                updateEvent();
            }
        });
    }

    /**
     * Loading the event details from the database
     * (The old info)
     */
    private void loadEventDetails() {

        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {

            @Override
            public void onEventLoaded(Event event) {
                if (!isAdded()) return;

                // Privacy
                privacy_Switch.setText(event.getPrivacy());
                if (event.getPrivacy() == "private"){
                    privacy_Switch.setChecked(true);
                }
                else {
                    privacy_Switch.setChecked(false);
                }

                // Title
                name.setText(event.getTitle());

                // Description
                descInput.setText(event.getDescription());

                // Place
                placeInput.setText((event.getPlace()));

                // Time
                String old_time = formatter.format(new Date(event.getDate()));
                timeInput.setText(old_time);

                // Reg Start
                if (event.getRegistrationStartDate() != 0) {
                    startRegInput.setText(formatter.format(new Date(event.getRegistrationStartDate())));
                }

                // Reg End
                if (event.getRegistrationEndDate() != 0) {
                    endRegInput.setText(formatter.format(new Date(event.getRegistrationEndDate())));
                }

                // Max Entrant
                if (event.getCapacity() != 0){
                    maxInput.setText(String.valueOf(event.getCapacity()));
                }
                // Waitlist count is handled by refreshWaitlistCount()

                // ADDED: show existing image if the event has one, and store it so save preserves it if no new image is picked
                if (event.getImageData() != null && !event.getImageData().isEmpty()) {
                    uploadedImageBase64 = event.getImageData();
                    ImageHelper.loadEventImage(previewImage, event);
                    previewImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(
                        getContext(),
                        "Failed to load event: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

            }
        });
    }

    private void showDateTimePicker(EditText targetInput) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(
                            getContext(),
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                targetInput.setText(format.format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );

                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
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
        String time = timeInput.getText().toString().trim();
        if (time.isEmpty()){
            Toast.makeText(getContext(), "Time cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        String start = startRegInput.getText().toString().trim();
        String end = endRegInput.getText().toString().trim();

        Date startDate = null;
        Date endDate = null;

        try {
            if (!start.isEmpty()) startDate = formatter.parse(start);
            if (!end.isEmpty()) endDate = formatter.parse(end);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            Toast.makeText(getContext(), "Registration Start is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) {
            Toast.makeText(getContext(), "Registration End is missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            Toast.makeText(getContext(), "Registration end must be after start", Toast.LENGTH_SHORT).show();
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
        String privacy = privacy_Switch.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String place = placeInput.getText().toString().trim();
        String timeStr = timeInput.getText().toString().trim();
        String start = startRegInput.getText().toString().trim();
        String end = endRegInput.getText().toString().trim();

        int capacity = 0;
        String maxEntrantsText = maxInput.getText().toString().trim();
        if (!maxEntrantsText.isEmpty()) {
            capacity = Integer.parseInt(maxEntrantsText);
        }

        Long timeMills = null;
        Long registrationStartMillis = null;
        Long registrationEndMillis = null;

        try {
            if (!timeStr.isEmpty()) {
                Date timeDate = formatter.parse(timeStr);
                if (timeDate != null) {
                    timeMills = timeDate.getTime();
                }
            }

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
                        "privacy", privacy,
                        "description", description,
                        "place", place,
                        "time", timeMills,
                        "capacity", capacity,
                        "registrationStartDate", registrationStartMillis == null ? 0L : registrationStartMillis,
                        "registrationEndDate", registrationEndMillis == null ? 0L : registrationEndMillis,
                        // ADDED: save image data, preserves existing if no new image was picked
                        "imageData", uploadedImageBase64
                )
                .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                            // direct back to EventDetailsFragment after updating
                            OrganizerEventNavigationFragment fragment = OrganizerEventNavigationFragment.newInstance(eventId);
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