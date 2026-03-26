package com.example.eventlotterysystem.UI.fragments;

import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // TODO: separate this firebase thingy.
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


    }
}
