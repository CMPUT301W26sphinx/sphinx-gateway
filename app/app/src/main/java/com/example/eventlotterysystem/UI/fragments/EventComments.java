package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.adapters.CommentAdapter;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EventComments extends Fragment {

    private static final String EVENT_ID = "event_id";

    private String eventId;

    private EditText writeCommentBox;
    private Button addCommentButton;
    private RecyclerView commentRecyclerView;

    private CommentAdapter commentAdapter;
    private List<UserComment> commentList;

    private final UserCommentManager commentManager = UserCommentManager.getInstance();
    private ListenerRegistration commentListener;

    public EventComments() {

    }

    public static EventComments newInstance(String eventId) {
        EventComments fragment = new EventComments();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(EVENT_ID);
        }

        writeCommentBox = view.findViewById(R.id.write_comment_box);
        addCommentButton = view.findViewById(R.id.add_comment_button);
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, false, listener -> {
            // do nothing
        });

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentRecyclerView.setAdapter(commentAdapter);

        if (eventId == null) {
            Toast.makeText(getContext(), "Missing event ID", Toast.LENGTH_SHORT).show();
            addCommentButton.setEnabled(false);
            return;
        }

        initializeCommentList();
        updateComments();
        addComment();

        Button backButton = view.findViewById(R.id.comment_back_button);

        backButton.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

    }

    private void initializeCommentList() {
        commentManager.getCommentsFromEvent(eventId, new UserCommentManager.UserCommentCallback() {
            @Override
            public void onCommentLoaded(List<UserComment> comments) {
                if (!isAdded()) return;

                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to get comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateComments() {
        commentListener = commentManager.listenToComments(eventId, new UserCommentManager.UserCommentCallback() {
            @Override
            public void onCommentLoaded(List<UserComment> comments) {
                if (!isAdded()) return;

                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        addCommentButton.setOnClickListener(v -> {
            String comment = writeCommentBox.getText().toString().trim();

            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(getContext(), "Enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            commentManager.addCommentToEvent(eventId, comment, new UserCommentManager.OnCommentAddedListener() {


                @Override
                public void onFailure(Exception e) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Void unused) {
                    writeCommentBox.setText("");
                    Toast.makeText(getContext(), "Comment added!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListener != null) {
            commentListener.remove();
        }
    }
}