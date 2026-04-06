package com.example.eventlotterysystem;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.UI.activities.AccountTypeActivity;
import com.example.eventlotterysystem.UI.fragments.CalendarFragment;
import com.example.eventlotterysystem.UI.fragments.EventListFragment;
import com.example.eventlotterysystem.UI.fragments.OrganizerFragment;
import com.example.eventlotterysystem.UI.fragments.ProfileFragment;
import com.example.eventlotterysystem.UI.fragments.QRCodeFragment;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    /**
     * Creates a new instance that goes to the Events, without login.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userSignIn();
    }

    /**
     * Signs the user in and creates their profile in firebase
     */
    public void userSignIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInAnonymously:success");

                    String uid = mAuth.getCurrentUser().getUid();
                    DocumentReference docRef = db.collection("users").document(uid);

                    docRef.get().addOnSuccessListener(document -> {
                        if (!document.exists()) {
                            ProfileManager profileManager = ProfileManager.getInstance();
                            UserProfile userProfile = new UserProfile();

                            profileManager.saveUser(userProfile, new ProfileManager.OnUserAddedCallback() {
                                @Override
                                public void onSuccess(Void snapshot) {
                                    Log.d(TAG, "User created successfully");
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Error creating user", e);
                                }
                            });
                        }

                        // Request location after user exists
                        requestLocationPermission();
                    });

                    // Setup bottom nav
                    initializeBottomNavigation();

                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                }
            }
        });
    }

    /**
     * Creates the bottom navigation
     */
    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        EventListFragment eventListFragment = new EventListFragment();
        QRCodeFragment qrCodeFragment = new QRCodeFragment();
        ProfileFragment profileFragment = new ProfileFragment();
        OrganizerFragment organizerFragment = new OrganizerFragment();

        // Set default fragment (Events)
        setCurrentFragment(eventListFragment);

        boolean showTermsPopup = getIntent().getBooleanExtra("show_terms_popup", false);
        if (showTermsPopup) {
            showTermsDialog();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.events) {
                setCurrentFragment(eventListFragment);
            } else if (id == R.id.profile) {
                setCurrentFragment(profileFragment);
            } else if (id == R.id.QRScan) {
                setCurrentFragment(qrCodeFragment);
            } else if (id == R.id.organizer) {
                setCurrentFragment(organizerFragment);
            } else if (id == R.id.calendar) { // ADDED
                setCurrentFragment(CalendarFragment.newInstance()); // ADDED
            } // ADDED
            return true;
        });
    }

    /**
     * Shows the terms and conditions pop-up
     */
    private void showTermsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_terms, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button understandBtn = dialogView.findViewById(R.id.btn_understand);
        understandBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            fetchAndSaveUserLocation();
        }
    }

    private void fetchAndSaveUserLocation() {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveUserLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(lastLocation -> {
                                    if (lastLocation != null) {
                                        saveUserLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
                                    }
                                });
                    }
                });
    }

    private void saveUserLocation(double lat, double lng) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        Map<String, Object> location = new HashMap<>();
        location.put("latitude", lat);
        location.put("longitude", lng);

        db.collection("users")
                .document(userId)
                .set(location, SetOptions.merge());
    }

    /**
     * Replaces current fragment with the specified fragment
     *
     * @param fragment the fragment to display next
     */
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAndSaveUserLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_switch_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch_account) {
            Intent intent = new Intent(this, AccountTypeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}