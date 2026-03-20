package com.example.eventlotterysystem.UI.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.LogItem;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<LogItem> logs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LogItem log);
    }

    public LogAdapter(List<LogItem> logs, OnItemClickListener listener) {
        this.logs = logs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem log = logs.get(position);
        holder.eventView.setText("Event: " + log.getEventName());
        holder.recipientView.setText("Recipient: " + log.getRecipientName());
        holder.messageView.setText("Message: " + log.getMessage());
        holder.timestampView.setText("Time: " + log.getTimestamp());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(log));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView eventView, recipientView, messageView, timestampView;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            eventView = itemView.findViewById(R.id.log_event);
            recipientView = itemView.findViewById(R.id.log_recipient);
            messageView = itemView.findViewById(R.id.log_message);
            timestampView = itemView.findViewById(R.id.log_timestamp);
        }
    }
}