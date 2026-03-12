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

    // Bottom navigation used to switch between admin pages.
    private BottomNavigationView bottomNavigationView;

    /**
     * Sets up the admin home screen and loads the default page.
     * The Events fragment is shown first when the admin opens this screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);

        // Connect the bottom navigation view from the layout.
        bottomNavigationView = findViewById(R.id.admin_bottom_nav);

        // Show the Events page by default when the admin enters this screen.
        loadFragment(new AdminEventsFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        // Change the visible fragment when a navigation item is selected.
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
            // Inside onCreateView, after finding buttons:

            return false;
        });
    }

    /**
     * Replaces the current fragment shown in the admin content area.
     *
     * @param fragment the fragment that should be displayed
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}