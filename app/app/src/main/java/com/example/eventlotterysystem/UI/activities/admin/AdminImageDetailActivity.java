package com.example.eventlotterysystem.UI.activities.admin;

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

        image = (ImageItem) getIntent().getSerializableExtra("image");
        if (image == null) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new EventImageRepository(); // or use dependency injection

        ImageView detailImage = findViewById(R.id.detail_image);
        TextView titleView = findViewById(R.id.detail_title);
        TextView descView = findViewById(R.id.detail_description);
        TextView uploaderView = findViewById(R.id.detail_uploader);
        Button backButton = findViewById(R.id.back_button);
        Button removeButton = findViewById(R.id.remove_button);

        // For now, use placeholder; later load from image.getImageUrl()
        detailImage.setImageResource(android.R.drawable.ic_menu_gallery);
        titleView.setText(image.getTitle());
        descView.setText(image.getDescription());
        uploaderView.setText("Uploaded by: " + image.getUploaderName());

        backButton.setOnClickListener(v -> finish());

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