package com.example.eventlotterysystem.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.ProfileManager;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.model.LotterySystem;
import com.example.eventlotterysystem.model.Notification;
import com.example.eventlotterysystem.model.profiles.UserProfile;

import java.util.List;

/**
 * Adapter for displaying event invitations in RecyclerViews.
 */
public class EventInviteAdapter extends RecyclerView.Adapter<EventInviteAdapter.ViewHolder> {

    private final List<Event> events;

    public EventInviteAdapter(List<Event> events) {
        this.events = events;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventName;
        private final TextView eventDescription;
        private final Button acceptButton;
        private final Button declineButton;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            eventName = (TextView) view.findViewById(R.id.EventName);
            eventDescription = (TextView) view.findViewById(R.id.EventDescription);
            acceptButton = (Button) view.findViewById(R.id.btnAccept);
            declineButton = (Button) view.findViewById(R.id.btnDecline);


        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.invite_event_recycler_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Event event = events.get(position);
        EntrantListFirebase db = new EntrantListFirebase();
        ProfileManager profileManager = ProfileManager.getInstance();

        // Bind data
        holder.eventName.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());

        // Button click listeners
        holder.acceptButton.setOnClickListener(v -> {
            // update user status to registered
            db.updateStatus(event.getEventId(), profileManager.getUserID(), EntrantListEntry.STATUS_REGISTERED);
            events.remove(position);
            notifyItemRemoved(position);

        });

        holder.declineButton.setOnClickListener(v -> {
            // update user status to declined
            db.updateStatus(event.getEventId(), profileManager.getUserID(), EntrantListEntry.STATUS_CANCELLED_OR_REJECTED);
            events.remove(position);
            notifyItemRemoved(position);
        });
    }


    @Override
    public int getItemCount() {
        return events.size();
    }
}
