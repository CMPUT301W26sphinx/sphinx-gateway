package com.example.eventlotterysystem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ProfileManagerTest {

    @Before
    public void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                latch.countDown();
                fail("signInAnonymously failed: " + task.getException());
                return;
            }

            assertNotNull(mAuth.getCurrentUser());
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    latch.countDown();
                    return;
                }

                UserProfile userProfile = new UserProfile();
                userProfile.setUserID(uid);
                userProfile.setFirstName("TestUser");


                db.collection("users").document(uid).set(userProfile).addOnSuccessListener(unused -> latch.countDown()).addOnFailureListener(e -> {
                    latch.countDown();
                    fail("Profile creation failed: " + e.getMessage());
                });
            }).addOnFailureListener(e -> {
                latch.countDown();
                fail("Profile lookup failed: " + e.getMessage());
            });
        });

        assertTrue("Timed out during setup", latch.await(30, TimeUnit.SECONDS));
    }

    ProfileManager manager = ProfileManager.getInstance();

    UserProfile user = new UserProfile("John", "Smith", "test@gmail.com", "123-456-7890");

    


}
