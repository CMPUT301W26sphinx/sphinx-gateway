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
 * Adapter for organizer event list (Manage Events screen).
 * Displays event name and description only.
 */
public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public OrganizerAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;
        TextView eventDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.eventName);
            eventDescription = itemView.findViewById(R.id.eventDescription);
        }
    }

    @NonNull
    @Override
    public OrganizerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false); // reuse same layout

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerAdapter.ViewHolder holder, int position) {

        Event event = eventList.get(position);

        holder.eventName.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());

        holder.itemView.setOnClickListener(v -> {
            listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
