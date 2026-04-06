package com.example.eventlotterysystem.UI.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.utils.ImageHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This activity is used to display the details of an event.
 * @author Hassan
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    private String eventId;
    private EventRepository repository;
    private TextView titleView, descView, regPeriodView, waitingListView;
    private ImageView posterView;
    private Button backButton, removeButton, viewCommentsButton;

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
        viewCommentsButton = findViewById(R.id.view_comments_button);

        repository = new EventRepository();
        loadEvent();

        backButton.setOnClickListener(v -> finish());

        removeButton.setOnClickListener(v -> deleteEvent());

        viewCommentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEventDetailActivity.this, AdminEventCommentsActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });
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

                if (event.getImageData() != null) {
                    Log.d("AdminEventDetail", "Image data present, length: " + event.getImageData().length());
                } else {
                    Log.d("AdminEventDetail", "Image data is null");
                }

                ImageHelper.loadEventImage(posterView, event);
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