package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.eventlotterysystem.model.CsvExporter;
import com.example.eventlotterysystem.model.LotterySystem;
import com.example.eventlotterysystem.model.Notification;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.EntrantAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.model.EntrantDisplay;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.database.ProfileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 * This fragment allows an organizer to view the entrants for an event according to their status
 * Use the {@link OrganizerEventEntrantsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerEventEntrantsFragment extends Fragment {

    private RecyclerView waitlistRecyclerView;
    private RecyclerView selectedRecyclerView;
    private RecyclerView enrolledRecyclerView;
    private RecyclerView cancelledRecyclerView;

    private EntrantAdapter waitlistAdapter;
    private EntrantAdapter selectedAdapter;
    private EntrantAdapter enrolledAdapter;
    private EntrantAdapter cancelledAdapter;

    private Button startLotteryButton;
    private Button sampleButton;
    private EditText sampleAmountInput;

    private Button notifyWaitlistButton;
    private Button notifySelectedButton;
    private Button notifyCancelledButton;
    private Button exportCsvButton;
    // To figure out how to create a csv, I looked at these sources:
    // https://medium.com/@sanjayajosep/offline-first-challenge-making-csv-pdf-reports-right-on-android-faf2ee7946dc
    private List<EntrantDisplay> enrolledList = new ArrayList<>();
    private ActivityResultLauncher<String> createCsvLauncher;
    private CsvExporter entrantCsvExporter;

    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
    private final ProfileManager profileManager = ProfileManager.getInstance();
    private final Notification notification = new Notification();
    private final LotterySystem lotterySystem = new LotterySystem();

    public OrganizerEventEntrantsFragment() {
        // Required empty public constructor
    }

    public static OrganizerEventEntrantsFragment newInstance(String eventId) {
        OrganizerEventEntrantsFragment fragment = new OrganizerEventEntrantsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }
    //https://medium.com/@sanjayajosep/offline-first-challenge-making-csv-pdf-reports-right-on-android-faf2ee7946dc
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri == null || !isAdded()) { // uri is where to put the file
                        return;
                    }
                    try {
                        entrantCsvExporter.writeCsv(uri, enrolledList);
                        Toast.makeText(requireContext(), "CSV exported", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_org_event_entrants, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize the csv
        entrantCsvExporter = new CsvExporter(requireContext());
        waitlistRecyclerView = view.findViewById(R.id.waitlistRecyclerView);
        selectedRecyclerView = view.findViewById(R.id.selectedRecyclerView);
        enrolledRecyclerView = view.findViewById(R.id.enrolledRecyclerView);
        cancelledRecyclerView = view.findViewById(R.id.cancelledRecyclerView);

        startLotteryButton = view.findViewById(R.id.startLotteryButton);
        sampleButton = view. findViewById(R.id.sampleButton);
        sampleAmountInput = view.findViewById(R.id.sampleAmountInput);

        notifyWaitlistButton = view.findViewById(R.id.notifyWaitlistButton);
        notifySelectedButton = view.findViewById(R.id.notifySelectedButton);
        notifyCancelledButton = view.findViewById(R.id.notifyCancelledButton);
        exportCsvButton = view.findViewById(R.id.exportCsvButton);

        notifyWaitlistButton.setOnClickListener(v -> {
            String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
            if (eventId == null) return;
            showNotifyDialog("Notify Waitlist", message ->
                    notification.notifyAllWaiting(message, eventId));
        });

        notifySelectedButton.setOnClickListener(v -> {
            String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
            if (eventId == null) return;
            showNotifyDialog("Notify Selected", message ->
                    notification.notifyAllSelected(message, eventId));
        });

        notifyCancelledButton.setOnClickListener(v -> {
            String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
            if (eventId == null) return;
            showNotifyDialog("Notify Cancelled", message ->
                    notification.notifyAllCancelled(message, eventId));
        });

        startLotteryButton.setOnClickListener(v -> {
            String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
            if (eventId == null) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Start Lottery")
                    .setMessage("This will run the lottery up to full capacity and notify all losers. Continue?")
                    .setPositiveButton("Start", (dialog, which) -> {
                        lotterySystem.firstLottery(eventId);
                        Toast.makeText(requireContext(), "Lottery started!", Toast.LENGTH_SHORT).show();
                        loadEntrants(eventId);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        sampleButton.setOnClickListener(v -> {
            String eventId = getArguments() != null ? getArguments().getString("eventId") : null;
            if (eventId == null) return;

            String input = sampleAmountInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a sample amount", Toast.LENGTH_SHORT).show();
                return;
            }

            int sampleAmount = Integer.parseInt(input);
            if (sampleAmount <= 0) {
                Toast.makeText(requireContext(), "Sample amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            lotterySystem.sampleLottery(eventId, sampleAmount);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Start Lottery")
                    .setMessage("This will try run the lottery by "+ sampleAmount +". Continue?")
                    .setPositiveButton("Start", (dialog, which) -> {
                        lotterySystem.firstLottery(eventId);
                        Toast.makeText(requireContext(), "Sampling Started!", Toast.LENGTH_SHORT).show();
                        loadEntrants(eventId);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            loadEntrants(eventId);
        });

        // TODO: CSV export
        exportCsvButton.setOnClickListener(v -> exportCsv());


        setupRecyclerViews();

        String eventId = null;
        if (getArguments() != null) {eventId = getArguments().getString("eventId");}
        if (eventId == null || eventId.isEmpty()) {Toast.makeText(requireContext(), "Missing event ID", Toast.LENGTH_SHORT).show();return;}
        loadEntrants(eventId);

    }
    /**
     * Notify dialog popup, for the custom messages that organizers can send.
     * @param title title of the dialog
     * @param action action of what the notify gon be
     * @Author Bryan Jonathan
     */
    private void showNotifyDialog(String title, NotifyAction action) {
        EditText input = new EditText(requireContext());
        input.setHint("Enter your message");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    action.notify(message);
                    Toast.makeText(requireContext(), "Notification sent!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private interface NotifyAction {
        void notify(String message);
    }

    /**
     * This method is used to load the entrants for the event.
     *  No parameters or returns.
     */
    private void setupRecyclerViews() {
        // I read about the multiple recycler views here, https://www.geeksforgeeks.org/android/how-to-create-a-nested-recyclerview-in-android/
        waitlistAdapter = new EntrantAdapter();
        selectedAdapter = new EntrantAdapter();
        enrolledAdapter = new EntrantAdapter();
        cancelledAdapter = new EntrantAdapter();

        // Set up layout managers for each RecyclerView
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        enrolledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cancelledRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        waitlistRecyclerView.setAdapter(waitlistAdapter);
        selectedRecyclerView.setAdapter(selectedAdapter);
        enrolledRecyclerView.setAdapter(enrolledAdapter);
        cancelledRecyclerView.setAdapter(cancelledAdapter);

            }

    /**
     * This method is used to load the entrants for the event.
     * @param eventId
     *  The id of the event to load the entrants for.
     *  No parameters or returns.
     */
    private void loadEntrants(String eventId){
        entrantListFirebase.getEntrantList(eventId)
                .addOnSuccessListener(entries -> {
                    if (!isAdded()) return;
                    if (entries == null || entries.isEmpty()) {
                        waitlistAdapter.setEntrants(new ArrayList<>());
                        selectedAdapter.setEntrants(new ArrayList<>());
                        enrolledAdapter.setEntrants(new ArrayList<>());
                        cancelledAdapter.setEntrants(new ArrayList<>());
                        return;
                    }

                    List<EntrantDisplay> waitlist = new ArrayList<>();
                    List<EntrantDisplay> selected = new ArrayList<>();
                    List<EntrantDisplay> enrolled = new ArrayList<>();
                    List<EntrantDisplay> cancelled = new ArrayList<>();

                    final int totalEntries = entries.size();
                    final int[] completedCount = {0}; // count helps with the firebase delay loading stuff in

                    for (EntrantListEntry entry : entries) {
                        profileManager.getUserProfileById(entry.getEntrantId(), user -> {
                            if (!isAdded()) return;

                            EntrantDisplay display = new EntrantDisplay(
                                    entry.getEntrantId(),
                                    user != null ? user.getFirstName() : null,
                                    user != null ? user.getLastName() : null,
                                    user != null ? user.getEmail() : null,
                                    entry.getStatus()
                            );

                            switch (entry.getStatus()) {
                                case EntrantListEntry.STATUS_WAITLIST:
                                    waitlist.add(display);
                                    break;

                                case EntrantListEntry.STATUS_INVITED:
                                    selected.add(display);
                                    break;

                                case EntrantListEntry.STATUS_REGISTERED:
                                    enrolled.add(display);
                                    // add entry to list for csv
                                    enrolledList.add(display);
                                    break;

                                case EntrantListEntry.STATUS_CANCELLED_OR_REJECTED:
                                    cancelled.add(display);
                                    break;
                            }

                            completedCount[0]++;

                            if (completedCount[0] == totalEntries) {
                                waitlistAdapter.setEntrants(waitlist);
                                selectedAdapter.setEntrants(selected);
                                enrolledAdapter.setEntrants(enrolled);
                                cancelledAdapter.setEntrants(cancelled);
                            }
                        });
                    }
                }).addOnFailureListener(e -> {if (!isAdded()) return;Toast.makeText(requireContext(), "Failed to load entrants", Toast.LENGTH_SHORT).show();});
    }

    private void exportCsv() {
        if (enrolledList == null || enrolledList.isEmpty()) {
            Toast.makeText(requireContext(), "No registered entrants", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isAdded()) return;
        createCsvLauncher.launch("registered_entrants.csv");
    }
}