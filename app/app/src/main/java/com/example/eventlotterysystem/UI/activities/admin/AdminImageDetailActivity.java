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
import com.example.eventlotterysystem.database.EventImageRepository;
import com.example.eventlotterysystem.database.ImageRepository;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.model.ImageItem;
import com.example.eventlotterysystem.utils.ImageHelper;

public class AdminImageDetailActivity extends AppCompatActivity {

    private ImageItem image;
    private ImageRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_detail);

        image = (ImageItem) getIntent().getSerializableExtra("image");
        if (image == null) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Log the presence of image data
        Log.d("AdminImageDetail", "Image data length: " +
                (image.getImageData() != null ? image.getImageData().length() : "null"));

        repository = new EventImageRepository();

        ImageView detailImage = findViewById(R.id.detail_image);
        TextView titleView = findViewById(R.id.detail_title);
        TextView descView = findViewById(R.id.detail_description);
        TextView uploaderView = findViewById(R.id.detail_uploader);
        Button backToListBtn = findViewById(R.id.back_to_list_button);
        Button viewEventBtn = findViewById(R.id.back_button);
        Button removeBtn = findViewById(R.id.remove_button);

        backToListBtn.setOnClickListener(v -> finish());

        titleView.setText(image.getTitle());
        descView.setText("From Event: " + image.getTitle());
        uploaderView.setText("Uploaded by: " + image.getUploaderName());

        // Load image using helper – create a dummy Event with the image data
        Event dummyEvent = new Event();
        dummyEvent.setEventId(image.getId());
        dummyEvent.setImageData(image.getImageData());
        ImageHelper.loadEventImage(detailImage, dummyEvent);

        viewEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventDetailActivity.class);
            intent.putExtra("eventId", image.getId());
            intent.putExtra("eventTitle", image.getTitle());
            intent.putExtra("eventDescription", image.getDescription());
            startActivity(intent);
        });

        removeBtn.setOnClickListener(v -> {
            ((EventImageRepository) repository).removeImageData(image.getId(), new ImageRepository.DeleteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AdminImageDetailActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AdminImageDetailActivity.this, "Failed to remove image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}