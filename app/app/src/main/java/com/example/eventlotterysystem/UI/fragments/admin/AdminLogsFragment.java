package com.example.eventlotterysystem.UI.fragments.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.LogRepository;
import com.example.eventlotterysystem.model.LogItem;

import java.util.ArrayList;
import java.util.List;

public class AdminLogsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogAdapter adapter;
    private List<LogItem> allLogs = new ArrayList<>();
    private List<LogItem> filteredLogs = new ArrayList<>();
    private LogRepository repository;
    private EditText searchInput;

    public AdminLogsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_logs, container, false);

        recyclerView = view.findViewById(R.id.logs_recycler);
        searchInput = view.findViewById(R.id.search_logs);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LogAdapter(filteredLogs, this::showLogDetails);
        recyclerView.setAdapter(adapter);

        repository = new LogRepository();
        loadLogs();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadLogs() {
        repository.getAllLogs(new LogRepository.LogsCallback() {
            @Override
            public void onLogsLoaded(List<LogItem> logs) {
                if (getActivity() == null) return;
                allLogs.clear();
                allLogs.addAll(logs);
                filteredLogs.clear();
                filteredLogs.addAll(logs);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filter(String query) {
        filteredLogs.clear();
        if (query.isEmpty()) {
            filteredLogs.addAll(allLogs);
        } else {
            String lower = query.toLowerCase();
            for (LogItem log : allLogs) {
                if (log.getEventName().toLowerCase().contains(lower) ||
                        log.getRecipientName().toLowerCase().contains(lower) ||
                        log.getMessage().toLowerCase().contains(lower)) {
                    filteredLogs.add(log);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showLogDetails(LogItem log) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_log_details, null);

        TextView eventText = dialogView.findViewById(R.id.dialog_event);
        TextView recipientText = dialogView.findViewById(R.id.dialog_recipient);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        TextView timeText = dialogView.findViewById(R.id.dialog_time);

        eventText.setText(log.getEventName());
        recipientText.setText(log.getRecipientName());
        messageText.setText(log.getMessage());
        timeText.setText(log.getTimestamp());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Back", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.parseColor("#FFFFFF"));
        positiveButton.setTextSize(16);
        positiveButton.setAllCaps(false);
    }
}