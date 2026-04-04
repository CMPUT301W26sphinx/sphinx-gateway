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
import android.widget.ImageButton;
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

public class EventListFragment extends Fragment {

    RecyclerView recyclerView;
    EventAdapter adapter;
    List<Event> allEvents = new ArrayList<>();
    List<Event> filteredEvents = new ArrayList<>();
    Set<String> allCategories = new HashSet<>();
    FirebaseFirestore db;
    EventRepository repository;

    // Filter state
    private List<String> currentSelectedCategories = new ArrayList<>();
    private int currentMinCap = -1;
    private int currentMaxCap = -1;
    private long currentStartDate = 0;
    private long currentEndDate = 0;
    private String currentSearchQuery = "";

    // UI elements for search
    private EditText searchInput;
    private ImageButton clearSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.eventlist_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(filteredEvents, this::onEventClick);
        recyclerView.setAdapter(adapter);

        repository = new EventRepository();

        repository.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                allEvents.clear();
                allCategories.clear();
                for (Event event : events) {
                    if (!"Private".equals(event.getPrivacy())) {
                        allEvents.add(event);
                        if (event.getCategory() != null && !event.getCategory().isEmpty()) {
                            allCategories.add(event.getCategory());
                        }
                    }
                }
                // Apply all current filters (including search)
                applyAllFilters();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });

        // Search bar
        searchInput = view.findViewById(R.id.search_input);
        clearSearch = view.findViewById(R.id.clear_search);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                if (currentSearchQuery.isEmpty()) {
                    clearSearch.setVisibility(View.GONE);
                } else {
                    clearSearch.setVisibility(View.VISIBLE);
                }
                applyAllFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            currentSearchQuery = "";
            clearSearch.setVisibility(View.GONE);
            applyAllFilters();
        });

        // Toggle group
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.check(R.id.buttonAll);

        Button myEventsButton = view.findViewById(R.id.buttonMyEvents);
        myEventsButton.setOnClickListener(v -> {
            MyEventsNavigation fragment = new MyEventsNavigation();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        Button filterButton = view.findViewById(R.id.filterButton);
        Button filterSetButton = view.findViewById(R.id.filterSetButton);

        filterSetButton.setOnClickListener(v -> showFilterDialog());
        filterButton.setOnClickListener(v -> clearAllFilters());

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && checkedId == R.id.buttonAll) {
                clearAllFilters();
            }
        });
        toggleGroup.check(R.id.buttonAll);
    }

    private void onEventClick(Event event) {
        Bundle bundle = new Bundle();
        bundle.putString("event_id", event.getEventId());
        EventDetailsFragment detailsFragment = new EventDetailsFragment();
        detailsFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void applyAllFilters() {
        filteredEvents.clear();

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

            // Date filter
            if (matches && (currentStartDate > 0 || currentEndDate > 0)) {
                long regStart = event.getRegistrationStartDate();
                long regEnd = event.getRegistrationEndDate();
                if (currentStartDate > 0 && regStart < currentStartDate) matches = false;
                if (currentEndDate > 0 && regEnd > currentEndDate) matches = false;
            }

            // Keyword search
            if (matches && !currentSearchQuery.isEmpty()) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                if (!title.contains(currentSearchQuery.toLowerCase()) &&
                        !desc.contains(currentSearchQuery.toLowerCase())) {
                    matches = false;
                }
            }

            if (matches) {
                filteredEvents.add(event);
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
        currentSearchQuery = "";
        if (searchInput != null) searchInput.setText("");
        if (clearSearch != null) clearSearch.setVisibility(View.GONE);
        applyAllFilters();
    }

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
            Set<String> selectedCategories = new HashSet<>(currentSelectedCategories); // pre-select current
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