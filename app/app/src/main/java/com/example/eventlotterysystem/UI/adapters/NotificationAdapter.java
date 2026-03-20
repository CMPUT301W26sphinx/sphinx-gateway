package com.example.eventlotterysystem.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onClick(String rawNotification);
    }

    private final List<String> items;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<String> items, OnNotificationClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void setItems(List<String> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String raw     = items.get(position);
        String[] parts = raw.split("\\|", -1);

        holder.sender.setText(parts.length > 2 ? parts[2] : "System");
        holder.message.setText(parts.length > 0 ? parts[0] : raw);

        holder.itemView.setOnClickListener(v -> listener.onClick(raw));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView sender;
        final TextView message;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sender  = itemView.findViewById(R.id.text_sender_name);
            message = itemView.findViewById(R.id.text_notification_message);
        }
    }
    public String removeItem(int position) {
        String removed = items.remove(position);
        notifyItemRemoved(position);
        return removed;
    }
}
