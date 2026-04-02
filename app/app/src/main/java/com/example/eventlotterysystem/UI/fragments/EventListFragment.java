package com.example.eventlotterysystem.UI.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
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
import com.example.eventlotterysystem.UI.adapters.EventAdapter;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.Event;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment responsible for displaying a list of events.
 *
 * <p>This fragment retrieves event data from Firebase Firestore and displays
 * the events using a RecyclerView with the EventAdapter.</p>
 *
 * <p>When a user selects an event, the fragment navigates to
 * {@link EventDetailsFragment} to display detailed information about
 * the selected event.</p>
 */
public class EventListFragment extends Fragment {

    /** RecyclerView used to display the list of events */
    RecyclerView recyclerView;

    /** Adapter used to bind event data to RecyclerView items */
    EventAdapter adapter;

    /** List that stores all Event objects retrieved from Firestore (full list) */
    List<Event> allEvents = new ArrayList<>();

    /** List that stores filtered events to be displayed */
    List<Event> filteredEvents = new ArrayList<>();

    /** Set of unique categories extracted from events */
    Set<String> allCategories = new HashSet<>();

    /** Firebase Firestore instance used for retrieving event data (now via repository) */
    FirebaseFirestore db;

    /** Repository for event data */
    EventRepository repository;

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater used to inflate the layout
     * @param container Parent container
     * @param savedInstanceState Saved instance state
     * @return Inflated view for the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.eventlist_main, container, false);
    }

    /**
     * Called after the fragment view has been created.
     * Initializes RecyclerView, adapter, and retrieves event data from Firestore.
     *
     * @param view Fragment view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView and set layout manager
        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize event list and adapter
        adapter = new EventAdapter(filteredEvents, this::onEventClick);
        recyclerView.setAdapter(adapter);

        // Initialize repository
        repository = new EventRepository();

        // Retrieve all events from Firestore 
        repository.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                allEvents.clear();
                allCategories.clear();
                for (Event event : events) {
                    if (!"Private".equals(event.getPrivacy())){
                        allEvents.add(event);
                        if (event.getCategory() != null && !event.getCategory().isEmpty()) {
                            allCategories.add(event.getCategory());
                        }
                    }
                }
                filteredEvents.clear();
                filteredEvents.addAll(allEvents);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Initialize toggle button group and set default selection.
         * Currently defaults to displaying all events.
         */
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.check(R.id.buttonAll);

        // transition to the invited events fragment
        Button myEventsButton = view.findViewById(R.id.buttonMyEvents);
        myEventsButton.setOnClickListener(v -> {
            // Replace the current fragment with AcceptEventInviteFragment
            MyEventsNavigation fragment = new MyEventsNavigation();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment) // your container ID
                    .addToBackStack(null) // optional: allows back button to go back
                    .commit();
        });

        // Filter buttons
        Button filterButton = view.findViewById(R.id.filterButton);
        Button filterSetButton = view.findViewById(R.id.filterSetButton);

        filterSetButton.setOnClickListener(v -> showFilterDialog());
        filterButton.setOnClickListener(v -> {
            // Clear all filters – show all events
            applyFilters(new ArrayList<>(), -1, -1, 0, 0);
        });

        // "All" toggle resets filters
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && checkedId == R.id.buttonAll) {
                filterButton.performClick();
            }
        });
        toggleGroup.check(R.id.buttonAll);

        //NOTIFICATIONS!
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
    }

    /**
     * Handles click on an event item.
     * @param event the selected event
     */
    private void onEventClick(Event event) {
        // Pass selected event ID to details fragment
        Bundle bundle = new Bundle();
        bundle.putString("event_id", event.getEventId());

        EventDetailsFragment detailsFragment = new EventDetailsFragment();
        detailsFragment.setArguments(bundle);

        // Replace current fragment with EventDetailsFragment
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Applies the current filter criteria to the event list.
     *
     * @param selectedCategories list of selected category names
     * @param minCap minimum capacity (or -1 if not set)
     * @param maxCap maximum capacity (or -1 if not set)
     * @param startDate start date timestamp (or 0 if not set)
     * @param endDate end date timestamp (or 0 if not set)
     */
    private void applyFilters(List<String> selectedCategories, int minCap, int maxCap,
                              long startDate, long endDate) {
        filteredEvents.clear();

        for (Event event : allEvents) {
            boolean matches = true;

            // Category filter
            if (!selectedCategories.isEmpty()) {
                String eventCat = event.getCategory();
                if (eventCat == null || !selectedCategories.contains(eventCat)) {
                    matches = false;
                }
            }

            // Capacity filter
            if (matches && (minCap > 0 || maxCap > 0)) {
                int cap = event.getCapacity();
                if (minCap > 0 && cap < minCap) matches = false;
                if (maxCap > 0 && cap > maxCap) matches = false;
            }

            // Date filter (registration period)
            if (matches && (startDate > 0 || endDate > 0)) {
                long regStart = event.getRegistrationStartDate();
                long regEnd = event.getRegistrationEndDate();
                if (startDate > 0 && regStart < startDate) matches = false;
                if (endDate > 0 && regEnd > endDate) matches = false;
            }

            if (matches) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    /**
     * Shows or hides the empty state message based on whether filteredEvents is empty.
     */
    private void updateEmptyState() {
        TextView emptyState = getView().findViewById(R.id.empty_state);
        if (filteredEvents.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Displays a dialog where the user can set filter options:
     * - Categories (multi‑select with search)
     * - Capacity range
     * - Registration date range
     */
    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // References to all views
        EditText minCap = dialogView.findViewById(R.id.min_capacity);
        EditText maxCap = dialogView.findViewById(R.id.max_capacity);
        Button startDateBtn = dialogView.findViewById(R.id.start_date_button);
        Button endDateBtn = dialogView.findViewById(R.id.end_date_button);
        Button applyBtn = dialogView.findViewById(R.id.apply_button);
        Button clearBtn = dialogView.findViewById(R.id.clear_button);
        LinearLayout categoryContainer = dialogView.findViewById(R.id.category_container);



        // Date picker state
        long[] startMillis = {0};
        long[] endMillis = {0};

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

        // Handle categories
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
            Set<String> selectedCategories = new HashSet<>();
            CategoryAdapter categoryAdapter = new CategoryAdapter(
                    new ArrayList<>(allCategories), selectedCategories);
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

            // Clear button also resets categories
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

            // Apply button uses selectedCategories
            applyBtn.setOnClickListener(v -> {
                List<String> selected = new ArrayList<>(selectedCategories);
                int min = -1, max = -1;
                try {
                    min = Integer.parseInt(minCap.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                try {
                    max = Integer.parseInt(maxCap.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                applyFilters(selected, min, max, startMillis[0], endMillis[0]);
                dialog.dismiss();
            });
        }

        // If categories were empty, we still need apply/clear to work without categories
        if (allCategories.isEmpty()) {
            applyBtn.setOnClickListener(v -> {
                int min = -1, max = -1;
                try {
                    min = Integer.parseInt(minCap.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                try {
                    max = Integer.parseInt(maxCap.getText().toString().trim());
                } catch (NumberFormatException ignored) {}
                applyFilters(new ArrayList<>(), min, max, startMillis[0], endMillis[0]);
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
