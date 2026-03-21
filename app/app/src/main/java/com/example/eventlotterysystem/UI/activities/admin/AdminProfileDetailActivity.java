package com.example.eventlotterysystem.UI.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;

public class AdminProfileDetailActivity extends AppCompatActivity {

    private UserProfile profile;
    private ProfileManager profileManager;
    private TextView firstNameView, lastNameView, emailView, phoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_detail);

        // Get profile from intent
        profile = (UserProfile) getIntent().getSerializableExtra("profile");
        if (profile == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        profileManager = ProfileManager.getInstance();

        // Initialize views using your layout's IDs
        firstNameView = findViewById(R.id.profile_first_name);
        lastNameView = findViewById(R.id.profile_last_name);
        emailView = findViewById(R.id.profile_email);
        phoneView = findViewById(R.id.profile_phone);
        Button backButton = findViewById(R.id.back_button);
        Button removeButton = findViewById(R.id.remove_button);

        // Populate data
        firstNameView.setText(profile.getFirstName() != null ? profile.getFirstName() : "Not set");
        lastNameView.setText(profile.getLastName() != null ? profile.getLastName() : "Not set");
        emailView.setText(profile.getEmail() != null ? profile.getEmail() : "Not set");
        phoneView.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Not set");

        backButton.setOnClickListener(v -> finish());

        removeButton.setOnClickListener(v -> {
            profileManager.deleteUser(profile.getProfileID(), new ProfileManager.OnDeleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AdminProfileDetailActivity.this, "Profile removed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AdminProfileDetailActivity.this, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}