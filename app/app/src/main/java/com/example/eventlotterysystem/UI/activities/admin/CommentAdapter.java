package com.example.eventlotterysystem.UI.activities.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.UserComment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<UserComment> comments;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserComment comment);
    }

    public CommentAdapter(List<UserComment> comments, OnItemClickListener listener) {
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        UserComment comment = comments.get(position);
        String displayName = comment.getUserName() != null ? comment.getUserName() : "Unknown";
        if (comment.isOrganizer()) displayName += " (Organizer)";
        holder.userView.setText(displayName);
        holder.textView.setText(comment.getText());
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.timeView.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            holder.timeView.setText("");
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(comment));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userView, textView, timeView;
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userView = itemView.findViewById(R.id.comment_user);
            textView = itemView.findViewById(R.id.comment_text);
            timeView = itemView.findViewById(R.id.comment_time);
        }
    }
}