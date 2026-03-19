package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.NotificationSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Seperate NotificationAdapter
 */
public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView subtitle;
    private NotificationSystem notificationSystem;
    private String userId;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

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
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
                subtitle.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setItems(notifications);
            }
        });
    }

    private static class NotificationAdapter
            extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private final List<String> items;

        NotificationAdapter(List<String> items) {
            this.items = items;
        }

        void setItems(List<String> newItems) {
            items.clear();
            items.addAll(newItems);
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
            holder.message.setText(items.get(position));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView message;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                message = itemView.findViewById(R.id.text_notification_message);
            }
        }
    }
}
