package com.example.eventlotterysystem.activities.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;

/**
 * Displays the details of a selected event for the admin.
 * This screen currently shows the event title and description
 * passed from the Events fragment.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    // TextViews used to display the selected event's information.
    TextView eventTitle;
    TextView eventDescription;

    /**
     * Sets up the event detail screen when the activity is opened.
     * Retrieves the event information passed through the intent
     * and displays it on the page.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        // Connect the TextViews from the XML layout to the Java code.
        eventTitle = findViewById(R.id.event_detail_title);
        eventDescription = findViewById(R.id.event_detail_description);

        // Get the event title and description sent from the previous screen.
        String title = getIntent().getStringExtra("eventTitle");
        String description = getIntent().getStringExtra("eventDescription");

        // Show the selected event information on the screen.
        eventTitle.setText(title);
        eventDescription.setText(description);

        // Temporary remove button behavior.
        // For now, this only shows a message.
        // Later, this can be connected to real remove logic.
        findViewById(R.id.remove_event_button).setOnClickListener(v ->
                Toast.makeText(this, "Remove Event clicked", Toast.LENGTH_SHORT).show()
        );
    }
}