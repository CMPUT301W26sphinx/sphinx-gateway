package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.NotificationAdapter;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.NotificationSystem;
import com.example.eventlotterysystem.model.EntrantListEntry;

import java.util.ArrayList;
/** Notification Fragment.
 * Pops up notification. Basically.
 * Calls NotificationSystem and NotificationAdapter.
 * @author Bryan Jonathan
 */
public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView subtitle;
    private NotificationSystem notificationSystem;
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationSystem = new NotificationSystem();

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        // Bind views
        recyclerView = view.findViewById(R.id.NotificationRecyclerView);
        subtitle     = view.findViewById(R.id.Notificationsubtitle);

        // Set up RecyclerView
        adapter = new NotificationAdapter(new ArrayList<>(), this::showNotificationDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeToDeleteCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;

                        String removed = adapter.removeItem(position);
                        notificationSystem.deleteNotification(userId, removed);
                    }
                };

        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerView);

        loadNotifications();
    }

    private void loadNotifications() {
        if (userId == null) {
            subtitle.setText("Could not see userID.");
            return;
        }

        notificationSystem.getNotifications(userId, notifications -> {
            if (notifications.isEmpty()) {
                subtitle.setText("You have no notifications.");
                recyclerView.setVisibility(View.GONE);
            } else {
                subtitle.setText("Swipe to delete, press to see more.");
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setItems(notifications);
            }
        });
    }

    /** Parser of the notifications so that is readable for Notification.
     * @param raw raw as f text that comes from NotificationSystem. Parser goes here.
     */
    private void showNotificationDialog(String raw) {
        String[] parts = raw.split("\\|", -1);
        String message  = parts.length > 0 ? parts[0] : raw;
        String eventId  = parts.length > 1 ? parts[1] : null;
        String sender   = parts.length > 2 ? parts[2] : "Notification";
        boolean isAsk  = parts.length > 3 && parts[3].equals("ask");

        if (isAsk) {
            showAskDialog(message, eventId, sender);
        } else {
            showStandardDialog(message, eventId, sender);
        }
    }
    private void showStandardDialog(String message, String eventId, String sender){
        new AlertDialog.Builder(requireContext())
                .setTitle(sender)
                .setMessage(message)
                .setPositiveButton("See More", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToEvent(eventId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAskDialog(String message, String eventId, String sender){
        new AlertDialog.Builder(requireContext())
                .setTitle(sender)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    entrantListFirebase.getEntry(eventId, userId).addOnSuccessListener(entry -> {
                        if (entry == null) {
                            EntrantListEntry newEntry = new EntrantListEntry(eventId, userId, EntrantListEntry.STATUS_WAITLIST);
                            entrantListFirebase.upsertEntry(eventId, newEntry).addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Joined waiting list. Welcome to the private event!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                    navigateToEvent(eventId);
                })
                .setNeutralButton("See More", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToEvent(eventId);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void navigateToEvent(String eventId) {
        if (eventId == null) return;
        EventDetailsFragment fragment = EventDetailsFragment.newInstance(eventId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        }
}
