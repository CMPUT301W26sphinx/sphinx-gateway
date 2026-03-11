package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;

public class ProfileEditFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_edit, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // display the current user information
        // update views
        TextView nameTextView = view.findViewById(R.id.edit_first_name);
        TextView emailTextView = view.findViewById(R.id.edit_email);
        TextView phoneTextView = view.findViewById(R.id.edit_phone_number);

        ProfileManager manager = new ProfileManager();

        manager.getUserProfile(user -> {
            if (user.getUserName() != null) {
                nameTextView.setText(user.getUserName());
            }

            if (user.getEmail() != null) {
                emailTextView.setText(user.getEmail());
            }

            if (user.getPhoneNumber() != null) {
                phoneTextView.setText(user.getPhoneNumber());
            }

        });

        // update the user profile based on typed info
        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            // update first name
            EditText userFirstNameInput = view.findViewById(R.id.edit_first_name);
            String firstName = userFirstNameInput.getText().toString();

            // display information saved message
            Toast myToast = Toast.makeText(getActivity(), "Information Saved!",
                    Toast.LENGTH_SHORT);
            myToast.show();
        });
    }
}
