package com.example.eventlotterysystem.UI.activity;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
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
    /**
     * Creates a new instance that goes to the Events, without login.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        EventListFragment eventListFragment = new EventListFragment();
        QRCodeFragment qrCodeFragment = new QRCodeFragment();
        ProfileFragment profileFragment = new ProfileFragment();

        //No login!
        setCurrentFragment(eventListFragment);

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

        // loads database and auth for firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // sign in the user
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            /**
             * TO DO: seperate this? not too sure.
             * @param task takes in mAuth, from firestore. If task is complete, then signin is success
             *             and will be passed to UID database.
             */
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInAnonymously:success");

                    // create profile
                    UserProfile userProfile = new UserProfile();

                    // save to Firestore
                    ProfileManager profileManager = new ProfileManager();
                    profileManager.saveUser(userProfile);

                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                }
            }
        });
    }

    /**
     * Replaces current fragment with the specified fragment
     * @param fragment the fragment to display next
     */
    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}