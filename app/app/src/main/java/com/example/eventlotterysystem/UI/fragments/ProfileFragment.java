package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;

public class ProfileFragment extends Fragment {
    /**
     * TO DO: Create Profile List Fragment!!
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
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

        // update views
        TextView nameTextView = view.findViewById(R.id.name);
        TextView emailTextView = view.findViewById(R.id.email);
        TextView phoneTextView = view.findViewById(R.id.phone_number);

        ProfileManager manager = new ProfileManager();

        manager.getUserProfile(user -> {
            nameTextView.setText("Name: " + user.getUserName());
            emailTextView.setText("Email: " + user.getEmail());
            phoneTextView.setText("Phone Number: " + user.getPhoneNumber());
        });
    }
}
