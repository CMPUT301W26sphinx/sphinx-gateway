package com.example.eventlotterysystem.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;

import java.util.List;

/**
 * Adapter class used to connect a list of Event objects to the RecyclerView.
 * This adapter inflates the event_item layout and binds event data
 * (event name and description) to the corresponding UI elements.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** List containing all events that will be displayed in the RecyclerView */
    private List<Event> eventList;

    /** Listener used to handle click events on individual event items */
    private OnEventClickListener listener;

    /**
     * Interface used to define click behavior when an event item is selected.
     */
    public interface OnEventClickListener {

        /**
         * Triggered when an event item is clicked.
         *
         * @param event The event that was selected.
         */
        void onEventClick(Event event);
    }

    /**
     * Constructor for the EventAdapter.
     *
     * @param eventList List of events that will be displayed.
     * @param listener Listener that handles event click interactions.
     */
    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    /**
     * ViewHolder class that holds references to the views for each event item.
     * This improves performance by avoiding repeated findViewById calls.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        /** TextView that displays the event name */
        TextView eventName;

        /** TextView that displays the event description */
        TextView eventDescription;

        /**
         * Constructor that initializes view references.
         *
         * @param itemView The layout view for a single event item.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.eventName);
            eventDescription = itemView.findViewById(R.id.eventDescription);

            // Example of commented-out debug line
            // System.out.println("EventViewHolder initialized");
        }
    }

    /**
     * Creates a new ViewHolder when RecyclerView needs one.
     * This method inflates the event_item layout.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of view.
     * @return A new EventViewHolder instance.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);

        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at the given position.
     *
     * @param holder The ViewHolder being updated.
     * @param position The position of the event in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {

        Event event = eventList.get(position);

        // Set event information to UI elements
        holder.eventName.setText(event.getEventName());
        holder.eventDescription.setText(event.getEventDescription());

        // Alternative debug example (commented out)
        // Log.d("EventAdapter", "Binding event: " + event.getEventName());

        // Handle click event for the item
        holder.itemView.setOnClickListener(v -> {
            listener.onEventClick(event);
        });
    }

    /**
     * Returns the total number of events in the dataset.
     *
     * @return Number of events in the list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }
}