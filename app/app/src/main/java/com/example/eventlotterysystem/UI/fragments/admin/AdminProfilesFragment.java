package com.example.eventlotterysystem.UI.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminProfileDetailActivity;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.profiles.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class AdminProfilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<UserProfile> allProfiles = new ArrayList<>();
    private List<UserProfile> filteredProfiles = new ArrayList<>();
    private ProfileManager profileManager;
    private EditText searchInput;
    private String currentFilter = null; // Track active filter

    public AdminProfilesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profiles, container, false);

        recyclerView = view.findViewById(R.id.profiles_recycler);
        searchInput = view.findViewById(R.id.search_profiles);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ProfileAdapter(filteredProfiles, this::openProfileDetail);
        recyclerView.setAdapter(adapter);

        profileManager = ProfileManager.getInstance();

        // Search filter
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentFilter = s.toString();
                filter(currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadProfiles();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfiles(); // Refresh when returning from detail activity
    }

    private void loadProfiles() {
        profileManager.getAllUsers(new ProfileManager.AllUsersCallback() {
            @Override
            public void onUsersLoaded(List<UserProfile> users) {
                if (getActivity() == null) return;

                allProfiles.clear();
                allProfiles.addAll(users);

                // Reapply current filter if any
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    filter(currentFilter);
                } else {
                    filteredProfiles.clear();
                    filteredProfiles.addAll(allProfiles);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filter(String query) {
        filteredProfiles.clear();
        if (query == null || query.isEmpty()) {
            filteredProfiles.addAll(allProfiles);
        } else {
            String lower = query.toLowerCase();
            for (UserProfile profile : allProfiles) {
                String firstName = profile.getFirstName() != null ? profile.getFirstName().toLowerCase() : "";
                String lastName = profile.getLastName() != null ? profile.getLastName().toLowerCase() : "";
                String fullName = (firstName + " " + lastName).trim();
                String email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";

                if (fullName.contains(lower) || email.contains(lower)) {
                    filteredProfiles.add(profile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openProfileDetail(UserProfile profile) {
        Intent intent = new Intent(getActivity(), AdminProfileDetailActivity.class);
        intent.putExtra("profile", profile);
        startActivity(intent);
    }
}