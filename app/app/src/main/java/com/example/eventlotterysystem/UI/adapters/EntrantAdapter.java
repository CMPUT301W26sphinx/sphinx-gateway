package com.example.eventlotterysystem.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.EntrantDisplay;
import com.example.eventlotterysystem.model.EntrantListEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying entrants in RecyclerViews.
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.ViewHolder> {

    private final List<EntrantDisplay> entrants = new ArrayList<>();
    public interface OnCancelClickListener {
        void onCancelClick(EntrantDisplay entrant);
    }

    private OnCancelClickListener cancelClickListener;

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }
    /**
     * Update the data shown in the list
     */
    public void setEntrants(List<EntrantDisplay> newEntrants) {
        entrants.clear();
        if (newEntrants != null) {
            entrants.addAll(newEntrants);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntrantDisplay entrant = entrants.get(position);

        // Set name
        holder.nameText.setText(entrant.getFullName());

        // Set email (with fallback)
        holder.emailText.setText(
                entrant.getEmail() != null ? entrant.getEmail() : "No email"
        );
        // Show cancel button only for invited entrants
        if (entrant.getStatus() == EntrantListEntry.STATUS_INVITED) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                if (cancelClickListener != null) {
                    cancelClickListener.onCancelClick(entrant);
                }
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
            holder.cancelButton.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * ViewHolder for each row
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        TextView emailText;
        Button cancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.profile_name);
            emailText = itemView.findViewById(R.id.profile_email);
            cancelButton = itemView.findViewById(R.id.cancelEntrantButton);
        }
    }
}