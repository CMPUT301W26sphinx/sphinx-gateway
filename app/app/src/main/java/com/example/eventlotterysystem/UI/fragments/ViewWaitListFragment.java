package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class ViewWaitListFragment extends Fragment {
    private static final String EVENT_ID = "event_id";
    private String eventId;
    private List<EntrantListEntry> waitlist = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ViewWaitListFragment() {}
    public static ViewWaitListFragment newInstance(String eventId) {
        ViewWaitListFragment fragment = new ViewWaitListFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_wait_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }
        loadWaitlist();
        showWaitList();
    }
    // Load the waiting list from data base
    private void loadWaitlist() {

        if (eventId == null) return;

        db.collection("events")
            .document(eventId)
            .collection("EntrantList")
            .whereEqualTo("status", EntrantListEntry.STATUS_WAITLIST) // status = 1
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {

                waitlist.clear();

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    EntrantListEntry entry = doc.toObject(EntrantListEntry.class);
                    waitlist.add(entry);
                }
        });
    }

    // To show all the entrant in the waiting list
    private void showWaitList() {
        LinearLayout waitlistContainer = getView().findViewById(R.id.waitlistContainer);

        // Clear all views (except the title)
        waitlistContainer.removeViews(1, waitlistContainer.getChildCount() - 1);

        if (waitlist.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("No Entrant in the waiting list.");
            tv.setTextSize(16f);
            tv.setPadding(8, 8, 8, 8);
            waitlistContainer.addView(tv);
            return;
        }

        // Generate TextView for each entrant in the waiting list
        for (EntrantListEntry entry : waitlist) {
            TextView tv = new TextView(getContext());
            tv.setText(entry.getEntrantId()); // replace by name later
            tv.setTextSize(18f);
            tv.setPadding(8, 8, 8, 8);
            waitlistContainer.addView(tv);
        }
    }
}
