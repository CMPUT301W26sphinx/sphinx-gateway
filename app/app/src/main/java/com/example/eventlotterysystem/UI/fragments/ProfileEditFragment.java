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
import com.example.eventlotterysystem.model.profiles.UserProfile;

public class ProfileEditFragment extends Fragment {
    /**
     * Allows the user to edit stored profile information such as: email, name, phone number.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_edit, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * Display the stored user information
         */
        // define views
        EditText userFirstNameInput = view.findViewById(R.id.edit_first_name);
        EditText userLastNameInput = view.findViewById(R.id.edit_last_name);
        EditText userEmailInput = view.findViewById(R.id.edit_email);
        EditText userPhoneInput = view.findViewById(R.id.edit_phone_number);

        // create profile manager
        ProfileManager manager = ProfileManager.getInstance();

        // update views with the users current information if the fields are non-null
        manager.getUserProfile(user -> {
            // update first name
            if (user.getFirstName() != null) {
                userFirstNameInput.setText(user.getFirstName());
            }
            // update last name
            if (user.getLastName() != null){
                userLastNameInput.setText(user.getLastName());
            }
            // update email
            if (user.getEmail() != null) {
                userEmailInput.setText(user.getEmail());
            }
            // update phone number
            if (user.getPhoneNumber() != null) {
                userPhoneInput.setText(user.getPhoneNumber());
            }

        });

        // update the user profile based on typed info when 'save' is pressed
        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            // get typed information
            String firstName = userFirstNameInput.getText().toString();
            String lastName = userLastNameInput.getText().toString();
            String email = userEmailInput.getText().toString();
            String phoneNumber = userPhoneInput.getText().toString();
            // new user profile
            UserProfile userProfile = new UserProfile();
            boolean isValid = true;

            // update fields
            if (email.contains("@") && email.endsWith(".com")){
                userProfile.setUserEmail(email);
            } else {
                Toast myToast = Toast.makeText(getActivity(), "Invalid email format",
                        Toast.LENGTH_SHORT);
                myToast.show();
                isValid = false;
            }

            if (phoneNumber.matches("\\d{3}-\\d{3}\\-\\d{4}") || phoneNumber.isEmpty()){
                userProfile.setUserPhoneNumber(phoneNumber);
            } else {
                Toast myToast = Toast.makeText(getActivity(), "Invalid phone number format",
                        Toast.LENGTH_SHORT);
                myToast.show();
                isValid = false;
            }

            if (firstName.isEmpty()){
                Toast myToast = Toast.makeText(getActivity(), "Name is a required field",
                        Toast.LENGTH_SHORT);
                myToast.show();
                isValid = false;
            } else {
                userProfile.setFirstName(firstName);
            }

            if (lastName.isEmpty()){
                Toast myToast = Toast.makeText(getActivity(), "Name is a required field",
                        Toast.LENGTH_SHORT);
                myToast.show();
                isValid = false;
            } else {
                userProfile.setLastName(lastName);

            }

            if (email.length() > 50){
                isValid = false;
                Toast myToast = Toast.makeText(getActivity(), "Email is too long",
                        Toast.LENGTH_SHORT);
                myToast.show();
            }
            // update user information in firebase
            if (isValid){
                manager.saveUser(userProfile, new ProfileManager.OnUserAddedCallback() {
                    @Override
                    public void onSuccess(Void snapshot) {
                        // display information saved message
                        Toast myToast = Toast.makeText(getActivity(), "Information Saved!",
                                Toast.LENGTH_SHORT);
                        myToast.show();
                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });

            }


        });
    }
}
