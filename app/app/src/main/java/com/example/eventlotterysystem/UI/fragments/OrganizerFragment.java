package com.example.eventlotterysystem.UI.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.OrganizerAdapter;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.Event;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrganizerFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button createEventButton;
    private OrganizerAdapter adapter;
    private List<Event> eventList;      // original list – now used as filtered list
    private List<Event> allEvents;      // new: full list from Firestore

    // Filter state variables
    private List<String> currentSelectedCategories = new ArrayList<>();
    private int currentMinCap = -1;
    private int currentMaxCap = -1;
    private long currentStartDate = 0;
    private long currentEndDate = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.organizer_eventlist, container, false);

        recyclerView = view.findViewById(R.id.OrganizerEventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize lists
        eventList = new ArrayList<>();
        allEvents = new ArrayList<>();

        adapter = new OrganizerAdapter(eventList, event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());

            OrganizerEventNavigationFragment fragment =
                    OrganizerEventNavigationFragment.newInstance(event.getEventId());

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        // Filter button click listener
        Button filterButton = view.findViewById(R.id.filterSetButton);
        filterButton.setOnClickListener(v -> showFilterDialog());

        Button clearButton = view.findViewById(R.id.clearFilterButton);
        clearButton.setOnClickListener(v -> clearAllFilters());

        createEventButton = view.findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(v -> {
            Fragment fragment = CreateEventFragment.newInstance();
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Notification button
        ProfileManager manager = ProfileManager.getInstance();
        view.findViewById(R.id.NotificationButton).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", manager.getUserID());

            NotificationFragment notificationFragment = new NotificationFragment();
            notificationFragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, notificationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadEvents();

        return view;

    }



    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = ProfileManager.getInstance().getUserID();

        // Query events where user is organizer
        db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .get()
                .continueWithTask(organizerTask -> {
                    if (!organizerTask.isSuccessful()) return Tasks.forException(organizerTask.getException());
                    QuerySnapshot organizerSnap = organizerTask.getResult();
                    // Query events where user is co-organizer
                    return db.collection("events")
                            .whereArrayContains("co_organizerIds", currentUserId)
                            .get()
                            .continueWith(coTask -> {
                                if (!coTask.isSuccessful()) return Tasks.forException(coTask.getException());
                                QuerySnapshot coSnap = coTask.getResult();
                                Set<String> seenIds = new HashSet<>();
                                allEvents.clear();
                                // Merge and deduplicate
                                for (QueryDocumentSnapshot doc : organizerSnap) {
                                    if (seenIds.add(doc.getId())) {
                                        Event event = doc.toObject(Event.class);
                                        event.setEventId(doc.getId());
                                        allEvents.add(event);
                                    }
                                }
                                for (QueryDocumentSnapshot doc : coSnap) {
                                    if (seenIds.add(doc.getId())) {
                                        Event event = doc.toObject(Event.class);
                                        event.setEventId(doc.getId());
                                        allEvents.add(event);
                                    }
                                }
                                return null;
                            });
                })
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        applyAllFilters(); // apply current filters (initially none)
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyAllFilters() {
        eventList.clear();
        for (Event event : allEvents) {
            boolean matches = true;

            // Category filter
            if (!currentSelectedCategories.isEmpty()) {
                String eventCat = event.getCategory();
                if (eventCat == null || !currentSelectedCategories.contains(eventCat)) {
                    matches = false;
                }
            }

            // Capacity filter
            if (matches && (currentMinCap > 0 || currentMaxCap > 0)) {
                int cap = event.getCapacity();
                if (currentMinCap > 0 && cap < currentMinCap) matches = false;
                if (currentMaxCap > 0 && cap > currentMaxCap) matches = false;
            }

            // Date filter (registration period)
            if (matches && (currentStartDate > 0 || currentEndDate > 0)) {
                long regStart = event.getRegistrationStartDate();
                long regEnd = event.getRegistrationEndDate();
                if (currentStartDate > 0 && regStart < currentStartDate) matches = false;
                if (currentEndDate > 0 && regEnd > currentEndDate) matches = false;
            }

            if (matches) {
                eventList.add(event);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void clearAllFilters() {
        currentSelectedCategories.clear();
        currentMinCap = -1;
        currentMaxCap = -1;
        currentStartDate = 0;
        currentEndDate = 0;
        // If you have a search query, also reset it: currentSearchQuery = "";
        applyAllFilters();
    }

    private void updateEmptyState() {
        // Optional: add a TextView with id 'empty_state' in your layout
        TextView emptyState = getView().findViewById(R.id.empty_state);
        if (emptyState != null) {
            if (eventList.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        }
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText minCap = dialogView.findViewById(R.id.min_capacity);
        EditText maxCap = dialogView.findViewById(R.id.max_capacity);
        Button startDateBtn = dialogView.findViewById(R.id.start_date_button);
        Button endDateBtn = dialogView.findViewById(R.id.end_date_button);
        Button applyBtn = dialogView.findViewById(R.id.apply_button);
        Button clearBtn = dialogView.findViewById(R.id.clear_button);
        LinearLayout categoryContainer = dialogView.findViewById(R.id.category_container);

        // Pre-fill with current values
        if (currentMinCap > 0) minCap.setText(String.valueOf(currentMinCap));
        if (currentMaxCap > 0) maxCap.setText(String.valueOf(currentMaxCap));

        long[] startMillis = {currentStartDate};
        long[] endMillis = {currentEndDate};

        if (currentStartDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startDateBtn.setText("Start: " + sdf.format(new java.util.Date(currentStartDate)));
        }
        if (currentEndDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            endDateBtn.setText("End: " + sdf.format(new java.util.Date(currentEndDate)));
        }

        startDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (startMillis[0] > 0) cal.setTimeInMillis(startMillis[0]);
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth);
                startMillis[0] = chosen.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                startDateBtn.setText("Start: " + sdf.format(chosen.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        endDateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (endMillis[0] > 0) cal.setTimeInMillis(endMillis[0]);
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth);
                endMillis[0] = chosen.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                endDateBtn.setText("End: " + sdf.format(chosen.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Extract distinct categories from allEvents
        Set<String> allCategories = new HashSet<>();
        for (Event e : allEvents) {
            if (e.getCategory() != null && !e.getCategory().isEmpty()) {
                allCategories.add(e.getCategory());
            }
        }

        if (allCategories.isEmpty()) {
            categoryContainer.removeAllViews();
            TextView noCats = new TextView(requireContext());
            noCats.setText("No categories available");
            noCats.setPadding(8, 8, 8, 8);
            categoryContainer.addView(noCats);
        } else {
            EditText searchCategory = dialogView.findViewById(R.id.search_category);
            RecyclerView categoryRecycler = dialogView.findViewById(R.id.category_recycler);
            categoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            categoryRecycler.setNestedScrollingEnabled(false);
            Set<String> selectedCategories = new HashSet<>(currentSelectedCategories);
            CategoryAdapter categoryAdapter = new CategoryAdapter(new ArrayList<>(allCategories), selectedCategories);
            categoryRecycler.setAdapter(categoryAdapter);

            searchCategory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    categoryAdapter.filter(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            clearBtn.setOnClickListener(v -> {
                selectedCategories.clear();
                categoryAdapter.filter("");
                categoryAdapter.notifyDataSetChanged();
                minCap.setText("");
                maxCap.setText("");
                startMillis[0] = 0;
                endMillis[0] = 0;
                startDateBtn.setText("Start Date");
                endDateBtn.setText("End Date");
                searchCategory.setText("");
            });

            applyBtn.setOnClickListener(v -> {
                currentSelectedCategories = new ArrayList<>(selectedCategories);
                try {
                    currentMinCap = Integer.parseInt(minCap.getText().toString().trim());
                } catch (NumberFormatException ignored) { currentMinCap = -1; }
                try {
                    currentMaxCap = Integer.parseInt(maxCap.getText().toString().trim());
                } catch (NumberFormatException ignored) { currentMaxCap = -1; }
                currentStartDate = startMillis[0];
                currentEndDate = endMillis[0];
                applyAllFilters();
                dialog.dismiss();
            });
        }

        if (allCategories.isEmpty()) {
            applyBtn.setOnClickListener(v -> {
                try {
                    currentMinCap = Integer.parseInt(minCap.getText().toString().trim());
                } catch (NumberFormatException ignored) { currentMinCap = -1; }
                try {
                    currentMaxCap = Integer.parseInt(maxCap.getText().toString().trim());
                } catch (NumberFormatException ignored) { currentMaxCap = -1; }
                currentStartDate = startMillis[0];
                currentEndDate = endMillis[0];
                applyAllFilters();
                dialog.dismiss();
            });

            clearBtn.setOnClickListener(v -> {
                minCap.setText("");
                maxCap.setText("");
                startMillis[0] = 0;
                endMillis[0] = 0;
                startDateBtn.setText("Start Date");
                endDateBtn.setText("End Date");
            });
        }

        dialog.show();
    }
}