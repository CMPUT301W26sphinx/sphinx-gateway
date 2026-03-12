package com.example.eventlotterysystem.UI.activities.admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.fragments.admin.AdminEventsFragment;
import com.example.eventlotterysystem.UI.fragments.admin.AdminImagesFragment;
import com.example.eventlotterysystem.UI.fragments.admin.AdminLogsFragment;
import com.example.eventlotterysystem.UI.fragments.admin.AdminProfilesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main home screen for the admin role.
 * This activity contains the bottom navigation bar and swaps
 * between the admin fragments for Events, Images, Profiles, and Logs.
 */
public class AdminHomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);

        bottomNavigationView = findViewById(R.id.admin_bottom_nav);

        loadFragment(new AdminEventsFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_events) {
                loadFragment(new AdminEventsFragment());
                return true;
            } else if (itemId == R.id.nav_images) {
                loadFragment(new AdminImagesFragment());
                return true;
            } else if (itemId == R.id.nav_profiles) {
                loadFragment(new AdminProfilesFragment());
                return true;
            } else if (itemId == R.id.nav_logs) {
                loadFragment(new AdminLogsFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}