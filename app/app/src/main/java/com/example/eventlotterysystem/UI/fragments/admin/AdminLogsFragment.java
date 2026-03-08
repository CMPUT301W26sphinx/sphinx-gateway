package com.example.eventlotterysystem.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;

/**
 * Fragment for the admin Logs page.
 * This page is intended to show notification or system logs
 * that the admin can review.
 */
public class AdminLogsFragment extends Fragment {

    /**
     * Required empty constructor for the fragment.
     */
    public AdminLogsFragment() {
    }

    /**
     * Loads the XML layout for the admin Logs page.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_logs, container, false);
    }
}