package com.example.eventlotterysystem.UI.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EventImageRepository;
import com.example.eventlotterysystem.database.ImageRepository;
import com.example.eventlotterysystem.model.ImageItem;

public class AdminImageDetailActivity extends AppCompatActivity {

    private ImageItem image;
    private ImageRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_detail);

        // Get the ImageItem from intent
        image = (ImageItem) getIntent().getSerializableExtra("image");
        if (image == null) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new EventImageRepository();

        // Initialize views
        TextView titleView = findViewById(R.id.detail_title);
        TextView descView = findViewById(R.id.detail_description);
        TextView uploaderView = findViewById(R.id.detail_uploader);
        ImageView detailImage = findViewById(R.id.detail_image);
        Button viewEventButton = findViewById(R.id.back_button);  // This is the "View Event" button
        Button removeButton = findViewById(R.id.remove_button);   // This is the "Remove Image" button

        // Populate data
        titleView.setText(image.getTitle());                      // Set event name as title
        descView.setText("From Event: " + image.getTitle());      // Show event name as source
        uploaderView.setText("Uploading User: " + image.getUploaderName()); // e.g., "System"
        detailImage.setImageResource(R.drawable.ic_images);       // Placeholder; replace later with actual image loading

        // View Event button: open AdminEventDetailActivity
        viewEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventDetailActivity.class);
            intent.putExtra("eventId", image.getId());            // Pass the event ID
            intent.putExtra("eventTitle", image.getTitle());
            intent.putExtra("eventDescription", image.getDescription());
            startActivity(intent);
        });

        // Remove Image button: delete the event/image
        removeButton.setOnClickListener(v -> {
            repository.deleteImage(image.getId(), new ImageRepository.DeleteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AdminImageDetailActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AdminImageDetailActivity.this, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}