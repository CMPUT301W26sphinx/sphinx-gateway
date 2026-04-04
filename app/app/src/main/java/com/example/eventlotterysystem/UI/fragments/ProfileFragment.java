package com.example.eventlotterysystem.UI.fragments;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.AlertDialog;
import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class ProfileFragment extends Fragment {
    /**
     * Provides a brief overview of available users functions, such as: edit profile, accept invitation, delete account
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_main, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // define views
        TextView nameTextView = view.findViewById(R.id.name);
        TextView emailTextView = view.findViewById(R.id.email);
        TextView phoneTextView = view.findViewById(R.id.phone_number);

        ProfileManager manager = ProfileManager.getInstance();

        // update views
        manager.getUserProfile(user -> {
            if (user.getFirstName() != null && user.getLastName() != null) {
                nameTextView.setText("Name: " + user.getFirstName() + " " + user.getLastName());
            }

            if (user.getEmail() != null) {
                emailTextView.setText("Email: " + user.getEmail());
            }

            if (user.getPhoneNumber() != null) {
                phoneTextView.setText("Phone Number: " + user.getPhoneNumber());
            }
        });

        view.findViewById(R.id.edit_button).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileEditFragment())
                    .addToBackStack(null)
                    .commit();
        });

        //Notification Fragment Button
        view.findViewById(R.id.NotificationMore).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", manager.getUserID());

            NotificationFragment notificationFragment = new NotificationFragment();
            notificationFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Checkbox for notification settings.
        CheckBox checkBox = view.findViewById(R.id.checkBoxforNotification);
        manager.getUserProfile(user -> {
            checkBox.setChecked(user.getNotificationPreference());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // save back to Firestore
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(manager.getUserID())
                        .update("notificationPreference", isChecked);
            });
        });


        //DELETE PROFILE BUTTON
        //Made it so that it writes null to all organizerId (because it may crash?)
        //Firebase SDK documentation is used yes yes
        //https://firebase.google.com/docs/firestore/manage-data/delete-data#java
        view.findViewById(R.id.deleteProfileButton).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete your profile? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
            String userId = manager.getUserID();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            //change organizer to null
            //set event to private (since it's over)
            db.collection("events")
                    .whereEqualTo("organizerId", userId)
                    .get()
                    .addOnSuccessListener(organizerEvents -> {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : organizerEvents) {
                            batch.update(doc.getReference(), "privacy","Private");
                            batch.update(doc.getReference(), "organizerId", null);
                        }
            //Find all events where user is in EntrantList subcollection
            db.collection("events")
                    .get()
                    .addOnSuccessListener(allEvents -> {
                        final int[] pendingTasks = {allEvents.size()};

                        if (allEvents.isEmpty()) {
                            // No events, commit batch and delete user
                            commitBatchAndDeleteUser(batch, db, userId);
                            return;
                        }
                for (DocumentSnapshot eventDoc : allEvents) {
                    eventDoc.getReference()
                            .collection("EntrantList")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(entrantDocs -> {
                                for (DocumentSnapshot entrantDoc : entrantDocs) {
                                    batch.delete(entrantDoc.getReference());
                                }
                                pendingTasks[0]--;
                                if (pendingTasks[0] == 0) {
                                    commitBatchAndDeleteUser(batch, db, userId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                pendingTasks[0]--;
                                if (pendingTasks[0] == 0) {
                                    commitBatchAndDeleteUser(batch, db, userId);
                                }
                            });
                        }
                    });
                });
                })
            .setNegativeButton("Cancel", null)
            .show();
        });
    }

    //Bye bye account
    //https://stackoverflow.com/questions/17719634/how-to-exit-an-android-app-programmatically
    private void commitBatchAndDeleteUser(WriteBatch batch, FirebaseFirestore db, String userId) {
        batch.delete(db.collection("users").document(userId));
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    FirebaseAuth.getInstance().getCurrentUser().delete()
                            .addOnSuccessListener(authVoid -> {
                                Toast.makeText(getContext(), "Profile deleted.", Toast.LENGTH_SHORT).show();
                                requireActivity().finishAffinity();
                            });
                });
    }
}