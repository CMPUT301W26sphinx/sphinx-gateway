package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class OrganizerCommentsFragment extends Fragment {
    private static final String EVENT_ID = "event_id";
    private EditText writeCommentBox;
    private String eventId;
    private Button addCommentButton;
    private RecyclerView commentRecyclerView;
    private ArrayList commentList;
    private CommentAdapter commentAdapter;
    private final UserCommentManager commentManager = UserCommentManager.getInstance();
    private ListenerRegistration commentListener;


    /**
     * Create Event Details fragment for specific event with eventId
     *
     * @param eventId The unique identifier for the event
     * @return fragment
     * A new instance of EventDetailsFragment
     */
    public static OrganizerCommentsFragment newInstance(String eventId) {
        /*
         Author: RobinHood https://stackoverflow.com/users/646806/robinhood
         Title: "How can I transfer data from one fragment to another fragment android"
         Answer: https://stackoverflow.com/a/19333288
         Date: Oct 12, 2013
         */
        OrganizerCommentsFragment fragment = new OrganizerCommentsFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    public OrganizerCommentsFragment() {
        super(R.layout.view_user_comments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(EVENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_user_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize recycler view
        writeCommentBox = view.findViewById(R.id.write_comment_box);
        addCommentButton = view.findViewById(R.id.add_comment_button);
        commentRecyclerView = view.findViewById(R.id.comment_recycler_view);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, true, commentID -> {
            commentManager.deleteComment(eventId, commentID, new UserCommentManager.OnCommentDeletedListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Comment could not be deleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                }
            });
        });

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentRecyclerView.setAdapter(commentAdapter);

        // populate recycler
        initializeCommentList();
        updateComments();
        addComment();


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

            commentManager.addCommentToEvent(eventId, comment, true, new UserCommentManager.OnCommentAddedListener() {


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
}
