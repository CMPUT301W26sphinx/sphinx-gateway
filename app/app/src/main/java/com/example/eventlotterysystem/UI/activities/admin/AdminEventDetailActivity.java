package com.example.eventlotterysystem.UI.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminEventDetailActivity extends AppCompatActivity {

    private String eventId;
    private EventRepository repository;
    private TextView titleView, descView, regPeriodView, waitingListView;
    private ImageView posterView;
    private Button backButton, removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: No event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        titleView = findViewById(R.id.event_detail_title);
        descView = findViewById(R.id.event_detail_description);
        regPeriodView = findViewById(R.id.event_registration_period);
        waitingListView = findViewById(R.id.event_waiting_list);
        posterView = findViewById(R.id.event_poster);
        backButton = findViewById(R.id.back_button);
        removeButton = findViewById(R.id.remove_event_button);

        repository = new EventRepository();
        loadEvent();

        backButton.setOnClickListener(v -> finish());

        removeButton.setOnClickListener(v -> deleteEvent());
    }

    private void loadEvent() {
        repository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                // Populate basic info
                titleView.setText(event.getTitle());
                descView.setText(event.getDescription());

                // Format registration period
                String regPeriod = formatRegistrationPeriod(
                        event.getRegistrationStartDate(),
                        event.getRegistrationEndDate());
                regPeriodView.setText(regPeriod);

                // Waiting list
                waitingListView.setText("Waiting List: " + event.getWaitingListCount() + " users");

                // TODO: Load poster image from URL (Glide or similar)
                // For now, keep placeholder
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminEventDetailActivity.this,
                        "Failed to load event: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String formatRegistrationPeriod(long start, long end) {
        if (start == 0 || end == 0) {
            return "Registration Period: Not set";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startStr = sdf.format(new Date(start));
        String endStr = sdf.format(new Date(end));
        return "Registration Period: " + startStr + " - " + endStr;
    }

    private void deleteEvent() {
        repository.removeEvent(eventId, new EventRepository.OnDeleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminEventDetailActivity.this,
                        "Event removed", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminEventDetailActivity.this,
                        "Failed to remove: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}