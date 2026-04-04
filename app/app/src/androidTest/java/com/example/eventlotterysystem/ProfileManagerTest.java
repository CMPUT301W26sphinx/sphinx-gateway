package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
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


    @Test
    public void testAddProfile() throws InterruptedException {
        ProfileManager manager = ProfileManager.getInstance();
        CountDownLatch latch = new CountDownLatch(1);

        UserProfile user = new UserProfile("Test", "User", "testUser@gmail.com", "123-456-7890");

        // save user
        manager.saveUser(user, new ProfileManager.OnUserAddedCallback() {
            @Override
            public void onSuccess(Void snapshot) {
                // retrieve information
                manager.getUserProfile(new ProfileManager.UserProfileCallBack() {
                    @Override
                    public void onComplete(UserProfile user) {
                        assertEquals("Test", user.getFirstName());
                        assertEquals("User", user.getLastName());
                        assertEquals("testUser@gmail.com", user.getEmail());
                        assertEquals("123-456-7890", user.getPhoneNumber());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                        fail("Failed to get profile: " + e.getMessage());
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Failed to add profile: " + e.getMessage());
            }
        });

        // delete profile
        final String uid = manager.getUserID();
        manager.deleteUser(uid, new ProfileManager.OnDeleteListener() {
            @Override
            public void onSuccess() {
                // verify delete
                manager.getUserProfile(new ProfileManager.UserProfileCallBack() {
                    @Override
                    public void onComplete(UserProfile user) {
                        latch.countDown();
                        fail("Failed to delete profile");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        latch.countDown();
                    }
                });
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
                fail("Delete profile: " + e.getMessage());
            }
        });

        assertTrue("Timed out waiting for add profile", latch.await(10, TimeUnit.SECONDS));

    }


}
