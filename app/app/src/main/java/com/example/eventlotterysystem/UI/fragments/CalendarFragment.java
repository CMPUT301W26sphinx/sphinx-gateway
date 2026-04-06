package com.example.eventlotterysystem.UI.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.CalendarEventAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import android.content.Intent;
import com.example.eventlotterysystem.UI.activities.admin.AdminEventDetailActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment that displays a calendar of all events and the user's registered events.
 * Dots are shown on dates that have events.
 * Tapping a date shows a list of events for that day below the calendar.
 */
public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private TextView selectedDateLabel;
    private Button allEventsButton;
    private Button myEventsButton;

    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    // All events loaded from Firestore
    private final List<Event> allEvents = new ArrayList<>();
    // Events the user is registered/waitlisted for
    private final List<Event> myEvents = new ArrayList<>();

    // Currently displayed set (all or my)
    private List<Event> activeEvents = new ArrayList<>();

    // Dates that have events in the active set
    private final Set<LocalDate> eventDates = new HashSet<>();

    // Events shown in the recycler for the selected date
    private final List<Event> eventsOnSelectedDate = new ArrayList<>();
    private CalendarEventAdapter adapter;

    private String currentUserId;
    private LocalDate selectedDate = null;

    // true = showing all events, false = showing my events
    private boolean showingAll = true;

    private boolean isAdminMode = false;

    public CalendarFragment() {}

    /**
     * Creates a new instance of CalendarFragment for entrant mode.
     * @return A new CalendarFragment instance.
     */
    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    /**
     * Creates a new instance of CalendarFragment for admin mode.
     * In admin mode, tapping an event navigates to AdminEventDetailActivity.
     * @return A new CalendarFragment instance with admin mode enabled.
     */
    public static CalendarFragment newInstanceAdmin() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_admin", true);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    /**
     * Called immediately after onCreateView has returned.
     * Initializes all UI elements, sets up the calendar, and loads events from Firestore.
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        eventsRecyclerView = view.findViewById(R.id.eventsOnDateRecyclerView);
        selectedDateLabel = view.findViewById(R.id.selectedDateLabel);
        allEventsButton = view.findViewById(R.id.allEventsButton);
        myEventsButton = view.findViewById(R.id.myEventsButton);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalendarEventAdapter(eventsOnSelectedDate, event -> {
            if (isAdminMode) {
                // ADDED: admin navigates to AdminEventDetailActivity
                Intent intent = new Intent(requireContext(), AdminEventDetailActivity.class);
                intent.putExtra("eventId", event.getEventId());
                startActivity(intent);
            } else {
                // Entrant navigates to EventDetailsFragment
                Fragment fragment = EventDetailsFragment.newInstance(event.getEventId());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        eventsRecyclerView.setAdapter(adapter);

        // Get current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        // ADDED: check if admin mode
        if (getArguments() != null) {
            isAdminMode = getArguments().getBoolean("is_admin", false);
        }

        setupCalendar();

        allEventsButton.setOnClickListener(v -> {
            showingAll = true;
            allEventsButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2196F3")));
            myEventsButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            activeEvents = allEvents;
            refreshEventDates();
            eventsOnSelectedDate.clear();
            adapter.notifyDataSetChanged();
            selectedDateLabel.setText("Select a date to see events");
        });

        myEventsButton.setOnClickListener(v -> {
            showingAll = false;
            myEventsButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2196F3")));
            allEventsButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            activeEvents = myEvents;
            refreshEventDates();
            eventsOnSelectedDate.clear();
            adapter.notifyDataSetChanged();
            selectedDateLabel.setText("Select a date to see events");
        });

        loadAllEvents();
    }

    /**
     * Sets up the kizitonwose CalendarView with day and month header binders.
     * Configures the calendar to show 3 months back and 12 months forward from the current month.
     */
    private void setupCalendar() {
        // Day binder — controls how each day cell looks
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getPosition() == DayPosition.MonthDate) {
                    container.textView.setAlpha(1f);

                    // Highlight selected date
                    if (day.getDate().equals(selectedDate)) {
                        container.textView.setTextColor(Color.WHITE);
                        container.textView.setBackgroundResource(R.drawable.calendar_dot);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackground(null);
                    }

                    // Show dot if date has events
                    if (eventDates.contains(day.getDate())) {
                        container.dotView.setVisibility(View.VISIBLE);
                    } else {
                        container.dotView.setVisibility(View.INVISIBLE);
                    }

                    // Handle date click
                    container.view.setOnClickListener(v -> {
                        selectedDate = day.getDate();
                        calendarView.notifyCalendarChanged();
                        showEventsOnDate(selectedDate);
                    });

                } else {
                    // Dim out-of-month dates
                    container.textView.setAlpha(0.3f);
                    container.dotView.setVisibility(View.INVISIBLE);
                    container.view.setOnClickListener(null);
                }
            }
        });

        // Month header binder — shows month name and day-of-week headers
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth month) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
                container.textView.setText(month.getYearMonth().format(formatter));
            }
        });

        // Show 3 months back and 12 months forward
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(3);
        YearMonth endMonth = currentMonth.plusMonths(12);

        calendarView.setup(startMonth, endMonth, java.time.DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);
    }

    /**
     * Rebuilds the set of dates that have events in the active list
     * and refreshes the calendar display.
     * Uses the device's default timezone to convert event timestamps to local dates.
     */
    private void refreshEventDates() {
        eventDates.clear();
        ZoneId deviceZone = ZoneId.systemDefault();
        for (Event event : activeEvents) {
            if (event.getDate() != 0) {
                LocalDate date = new Date(event.getDate())
                        .toInstant()
                        .atZone(deviceZone)
                        .toLocalDate();
                eventDates.add(date);
            }
        }
        calendarView.notifyCalendarChanged();
    }

    /**
     * Loads all events from Firestore, then loads the user's registered events.
     * Admin can see all events including private ones.
     * Entrants can only see public events in the all events view.
     * Sets the active events list to all events and refreshes the calendar.
     */
    private void loadAllEvents() {
        eventRepository.getEvents(new EventRepository.EventCallback() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (!isAdded()) return;
                allEvents.clear();
                for (Event event : events) {
                    if (isAdminMode) {
                        // ADDED: admin sees all events including private
                        allEvents.add(event);
                    } else {
                        // ADDED: entrant only sees public events in all events view
                        if (event.getPrivacy() == null || event.getPrivacy().equalsIgnoreCase("Public")) {
                            allEvents.add(event);
                        }
                    }
                }
                activeEvents = allEvents;
                refreshEventDates();
                loadMyEvents();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads events the current user is on the waitlist or registered for.
     * Since EntrantListFirebase is structured per event, we check each event individually.
     * Updates the calendar if the user is currently viewing their own events.
     */
    private void loadMyEvents() {
        if (currentUserId == null) return;

        myEvents.clear();
        final int totalEvents = allEvents.size();
        if (totalEvents == 0) return;

        final int[] completedChecks = {0};

        for (Event event : allEvents) {
            if (event.getEventId() == null) {
                completedChecks[0]++;
                continue;
            }
            entrantListFirebase.getEntry(event.getEventId(), currentUserId)
                    .addOnSuccessListener(entry -> {
                        if (!isAdded()) return;
                        if (entry != null && entry.getStatus() != EntrantListEntry.STATUS_CANCELLED_OR_REJECTED) {
                            myEvents.add(event);
                        }
                        completedChecks[0]++;
                        if (completedChecks[0] == totalEvents && !showingAll) {
                            refreshEventDates();
                        }
                    })
                    .addOnFailureListener(e -> {
                        completedChecks[0]++;
                    });
        }
    }

    /**
     * Filters the active events list to only those on the selected date
     * and updates the RecyclerView below the calendar.
     * Uses the device's default timezone to compare dates correctly.
     * @param date The selected date to filter events for.
     */
    private void showEventsOnDate(LocalDate date) {
        eventsOnSelectedDate.clear();
        ZoneId deviceZone = ZoneId.systemDefault();

        for (Event event : activeEvents) {
            if (event.getDate() != 0) {
                LocalDate eventDate = new Date(event.getDate())
                        .toInstant()
                        .atZone(deviceZone)
                        .toLocalDate();
                if (eventDate.equals(date)) {
                    eventsOnSelectedDate.add(event);
                }
            }
        }

        adapter.notifyDataSetChanged();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(date.atStartOfDay(deviceZone).toInstant().toEpochMilli()));

        if (eventsOnSelectedDate.isEmpty()) {
            selectedDateLabel.setText("No events on " + dateStr);
        } else {
            selectedDateLabel.setText("Events on " + dateStr + ":");
        }
    }

    /**
     * ViewContainer for each day cell in the calendar.
     * Holds references to the day number text view and the event dot indicator.
     */
    class DayViewContainer extends ViewContainer {
        TextView textView;
        View dotView;
        View view;

        /**
         * Constructs a DayViewContainer and binds the day text and dot views.
         * @param view The inflated day cell view.
         */
        DayViewContainer(@NonNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
            dotView = view.findViewById(R.id.calendarDayDot);
        }
    }

    /**
     * ViewContainer for the month header.
     * Holds a reference to the month title text view and wires up
     * the previous and next month navigation buttons.
     */
    class MonthViewContainer extends ViewContainer {
        TextView textView;

        /**
         * Constructs a MonthViewContainer and binds the month title and navigation buttons.
         * @param view The inflated month header view.
         */
        MonthViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarMonthText);

            // ADDED: previous and next month buttons
            view.findViewById(R.id.previousMonthButton).setOnClickListener(v -> {
                YearMonth firstVisible = calendarView.findFirstVisibleMonth() != null
                        ? calendarView.findFirstVisibleMonth().getYearMonth()
                        : YearMonth.now();
                calendarView.smoothScrollToMonth(firstVisible.minusMonths(1));
            });

            view.findViewById(R.id.nextMonthButton).setOnClickListener(v -> {
                YearMonth firstVisible = calendarView.findFirstVisibleMonth() != null
                        ? calendarView.findFirstVisibleMonth().getYearMonth()
                        : YearMonth.now();
                calendarView.smoothScrollToMonth(firstVisible.minusMonths(-1));
            });
        }
    }
}