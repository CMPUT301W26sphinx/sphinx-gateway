package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.fragments.admin.ProfileAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.model.Notification;
import com.example.eventlotterysystem.model.profiles.UserProfile;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InviteCoOrganizerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InviteCoOrganizerFragment extends Fragment {

    private String eventId;
    private static final String EVENT_ID = "event_id";
    private RecyclerView recyclerView;
    private ProfileManager profileManager;
    private ProfileAdapter adapter;
    private List<UserProfile> allProfiles = new ArrayList<>();
    private List<UserProfile> searchProfiles = new ArrayList<>();
    private EditText name;
    private EditText phoneNum;
    private EditText email;
    private Button searchButton;
    private Button backButton;
    private List<String> coOrganizer_List;
    private String organizer;
    private final Notification notification = new Notification();
    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantList = new EntrantListFirebase();

    public InviteCoOrganizerFragment() {}

    public static InviteCoOrganizerFragment newInstance(String eventId) {
        InviteCoOrganizerFragment fragment = new InviteCoOrganizerFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite_co_organizer, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }
        profileManager = ProfileManager.getInstance();
        recyclerView = view.findViewById(R.id.entrantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ProfileAdapter(searchProfiles, this::inviteCoOrganizer);
        recyclerView.setAdapter(adapter);
        name = view.findViewById(R.id.name);
        phoneNum = view.findViewById(R.id.phoneNum);
        email = view.findViewById(R.id.email);
        searchButton = view.findViewById(R.id.search_button);
        backButton = view.findViewById(R.id.backButton);

        loadProfiles();
        loadEvent();

        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        searchButton.setOnClickListener(v -> {
            searchProfiles.clear();
            search();
        });

    }
    /**
     * loading all the user porfile from the database
     */
    private void loadProfiles() {
        profileManager.getAllUsers(new ProfileManager.AllUsersCallback() {
            @Override
            public void onUsersLoaded(List<UserProfile> users) {
                if (getActivity() == null) return;

                allProfiles.clear();
                allProfiles.addAll(users);

            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load profiles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Loading the event from database
     * Aim to getting the organizer's Id and co-organizers' Id
     */
    private void loadEvent() {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {

            @Override
            public void onEventLoaded(Event event) {
                if (!isAdded()) return;
                organizer = event.getOrganizerId();
                coOrganizer_List = event.getCoOrganizerIds();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(
                        getContext(),
                        "Failed to load event: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
    /**
     * Search users base on given name, phone number and email
     */
    private void search() {
        String target_nameStr = name.getText().toString().trim().toLowerCase();
        String target_phoneNumStr = phoneNum.getText().toString().trim();
        String target_emailStr = email.getText().toString().trim().toLowerCase();
        for (UserProfile profile : allProfiles) {
            String data_firstName = profile.getFirstName() != null ? profile.getFirstName().toLowerCase() : "";
            String data_lastName = profile.getLastName() != null ? profile.getLastName().toLowerCase() : "";
            String data_fullName = (data_firstName + " " + data_lastName).trim();
            String data_email = profile.getEmail() != null ? profile.getEmail().toLowerCase() : "";
            String data_phoneNum = profile.getPhoneNumber() != null ? profile.getPhoneNumber().replace("-", "") : "";

            if (data_fullName.contains(target_nameStr)
                    && data_email.contains(target_emailStr)
                    && data_phoneNum.contains(target_phoneNumStr)){
                searchProfiles.add(profile);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /** adds Coorganizer to the event, and notifies them.
     * Uses the searchbar implemented by Ivan
     * @param profile
     * @author Bryan Jonathan
     */
    private void inviteCoOrganizer(UserProfile profile) {
        if (eventId == null) {
            Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_SHORT).show();
            return;
        }
        String entrantId = profile.getProfileID();
        EventRepository eventRepository = new EventRepository();
        EntrantListFirebase entrantList = new EntrantListFirebase();
        Notification notification = new Notification();
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                List<String> coOrganizerIds  = event.getCoOrganizerIds();
                if (coOrganizerIds != null && coOrganizerIds.contains(entrantId)) {
                    Toast.makeText(requireContext(),
                            profile.getFirstName() + " is already a coorganizer ",
                            Toast.LENGTH_SHORT).show();
                } else {
                    coOrganizerIds.add(entrantId);
                    entrantList.removeEntrantListEntry(eventId, entrantId);

                    FirebaseFirestore.getInstance()
                            .collection("events")
                            .document(eventId)
                            .update("coOrganizerIds", FieldValue.arrayUnion(entrantId))
                            .addOnSuccessListener(unused -> {
                                notification.notifyOrganizerInvite(entrantId, eventId);
                                Toast.makeText(requireContext(),
                                        profile.getFirstName() + " has been invited as a co-organizer.",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Failed to add co-organizer", Toast.LENGTH_SHORT).show()
                            );
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
