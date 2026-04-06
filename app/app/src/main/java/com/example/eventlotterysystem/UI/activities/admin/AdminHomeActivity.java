package com.example.eventlotterysystem.UI.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.AccountTypeActivity;
import com.example.eventlotterysystem.UI.fragments.CalendarFragment;
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
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

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
            } else if (itemId == R.id.nav_calendar) {
                loadFragment(CalendarFragment.newInstanceAdmin());
                return true;
            }

            return false;
        });

        // ADDED: listen to back stack changes to show/hide toolbar
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount == 0) {
                // Back to a main nav fragment, show toolbar and bottom nav
                toolbar.setVisibility(android.view.View.VISIBLE);
                bottomNavigationView.setVisibility(android.view.View.VISIBLE);
            } else {
                // Inside a detail fragment, hide toolbar and bottom nav
                toolbar.setVisibility(android.view.View.GONE);
                bottomNavigationView.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_switch_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch_account) {
            // Go back to account type selection and clear back stack
            Intent intent = new Intent(this, AccountTypeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}