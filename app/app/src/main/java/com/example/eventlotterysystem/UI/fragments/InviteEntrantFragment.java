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

    import java.util.ArrayList;
    import java.util.List;

    /**
     * A simple {@link Fragment} subclass.
     * Use the {@link InviteEntrantFragment#newInstance} factory method to
     * create an instance of this fragment.
     */
    public class InviteEntrantFragment extends Fragment {
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
        private String privacy;
        private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
        private final Notification notification = new Notification();
        public InviteEntrantFragment() {}

        public static InviteEntrantFragment newInstance(String eventId) {
            InviteEntrantFragment fragment = new InviteEntrantFragment();
            Bundle args = new Bundle();
            args.putString(EVENT_ID, eventId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_invite_entrant, container, false);
        }

        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Bundle args = getArguments();
            if (args != null) {
                eventId = args.getString(EVENT_ID);
            }

            // fetch privacy from event
            EventRepository eventRepository = new EventRepository();
            eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
                @Override
                public void onEventLoaded(Event event) {
                    privacy = event.getPrivacy() != null ? event.getPrivacy() : "Public";
                }

                @Override
                public void onError(Exception e) {
                    privacy = "Public";
                }
            });
            profileManager = ProfileManager.getInstance();
            recyclerView = view.findViewById(R.id.entrantsRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter = new ProfileAdapter(searchProfiles, this::inviteEntrant);
            recyclerView.setAdapter(adapter);
            name = view.findViewById(R.id.name);
            phoneNum = view.findViewById(R.id.phoneNum);
            email = view.findViewById(R.id.email);
            searchButton = view.findViewById(R.id.search_button);

            loadProfiles();
            searchButton.setOnClickListener(v -> {
                searchProfiles.clear();
                search();
            });

        }
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

        private void inviteEntrant(UserProfile profile) {
            if (eventId == null) {
                Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_SHORT).show();
                return;
            }
            String entrantId = profile.getProfileID();
            entrantListFirebase.getEntry(eventId, entrantId)
                    .addOnSuccessListener(entry -> {
                        if (entry != null) {
                            Toast.makeText(requireContext(),
                                    profile.getFirstName() + " is already in the entrant list",
                                    Toast.LENGTH_SHORT).show();
                        } else if ("Private".equalsIgnoreCase(privacy)){
                            notification.notifyPrivateInvite(entrantId,eventId);
                            Toast.makeText(requireContext(),
                                    profile.getFirstName() + " is now invited.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            notification.notifyInvite(entrantId, eventId);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to check entrant status", Toast.LENGTH_SHORT).show()
                    );
        }
    }

