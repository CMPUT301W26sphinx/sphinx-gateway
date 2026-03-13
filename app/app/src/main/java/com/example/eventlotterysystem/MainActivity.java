package com.example.eventlotterysystem;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.UI.fragments.EventListFragment;
import com.example.eventlotterysystem.UI.fragments.ProfileFragment;
import com.example.eventlotterysystem.UI.fragments.QRCodeFragment;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserProfile currentProfile;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        EventListFragment eventListFragment = new EventListFragment();
        QRCodeFragment qrCodeFragment = new QRCodeFragment();
        ProfileFragment profileFragment = new ProfileFragment();

        // Set default fragment (Events)
        setCurrentFragment(eventListFragment);

        // No Login!!
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.events) {
                setCurrentFragment(eventListFragment);
            } else if (id == R.id.profile) {
                setCurrentFragment(profileFragment);
            } else if (id == R.id.QRScan) {
                setCurrentFragment(qrCodeFragment);
            }
            return true;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Sign in anonymously
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInAnonymously:success");

                    FirebaseUser user = mAuth.getCurrentUser();
                    // No need to pass UID to constructor – ProfileManager will set it
                    UserProfile userProfile = new UserProfile(); // no-arg constructor
                    ProfileManager profileManager = new ProfileManager();
                    profileManager.saveUser(userProfile);

                    // Stay on entrant UI (no redirect)
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                }
            }
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
