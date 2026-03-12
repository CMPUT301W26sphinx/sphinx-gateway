package com.example.eventlotterysystem.UI.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;

/**
 * Fragment for the admin Images page.
 * This page will be used to browse uploaded images
 * and later support image moderation features.
 */
public class AdminImagesFragment extends Fragment {

    /**
     * Required empty constructor for the fragment.
     */
    public AdminImagesFragment() {
    }

    /**
     * Loads the XML layout for the admin Images page.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_images, container, false);
    }
}