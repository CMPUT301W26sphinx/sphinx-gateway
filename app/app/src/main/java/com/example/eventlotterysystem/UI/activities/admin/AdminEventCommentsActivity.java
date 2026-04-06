package com.example.eventlotterysystem.UI.activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.model.UserComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEventCommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<UserComment> commentList = new ArrayList<>();
    private UserCommentManager commentManager;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_comments);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Event ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.comments_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(commentList, this::showCommentDetail);
        recyclerView.setAdapter(adapter);

        commentManager = UserCommentManager.getInstance();
        loadComments();
    }

    private void loadComments() {
        commentManager.getCommentsFromEventWithIds(eventId, new UserCommentManager.UserCommentCallback() {
            @Override
            public void onCommentLoaded(List<UserComment> comments) {
                commentList.clear();
                commentList.addAll(comments);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminEventCommentsActivity.this,
                        "Failed to load comments: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCommentDetail(UserComment comment) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comment_detail, null);

        TextView userView = dialogView.findViewById(R.id.dialog_user);
        TextView organizerView = dialogView.findViewById(R.id.dialog_organizer);
        TextView commentView = dialogView.findViewById(R.id.dialog_comment);
        TextView timeView = dialogView.findViewById(R.id.dialog_time);

        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel);
        Button deleteButton = dialogView.findViewById(R.id.dialog_delete);

        userView.setText(comment.getUserName() != null ? comment.getUserName() : "Unknown");
        organizerView.setText(comment.getIsOrganizer() ? "Yes" : "No");  // ✅ fixed
        commentView.setText(comment.getText() != null ? comment.getText() : "");

        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            timeView.setText(sdf.format(comment.getTimestamp().toDate()));
        } else {
            timeView.setText("Unknown");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            deleteComment(comment);
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void deleteComment(UserComment comment) {
        commentManager.deleteComment(eventId, comment.getCommentID(), new UserCommentManager.OnCommentDeletedListener() {  // ✅ fixed
            @Override
            public void onSuccess() {
                Toast.makeText(AdminEventCommentsActivity.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                loadComments(); // refresh
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminEventCommentsActivity.this,
                        "Failed to delete comment: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}