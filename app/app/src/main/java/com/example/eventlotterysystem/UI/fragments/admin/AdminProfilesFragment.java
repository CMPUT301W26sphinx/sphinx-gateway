package com.example.eventlotterysystem.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;

/**
 * Fragment for the admin Profiles page.
 * This page will be used to browse entrant and organizer profiles
 * and later support profile moderation features.
 */
public class AdminProfilesFragment extends Fragment {

    /**
     * Required empty constructor for the fragment.
     */
    public AdminProfilesFragment() {
    }

    /**
     * Loads the XML layout for the admin Profiles page.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profiles, container, false);
    }
}