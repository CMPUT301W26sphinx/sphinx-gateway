package com.example.eventlotterysystem.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.UserCommentManager;
import com.example.eventlotterysystem.model.UserComment;
import com.google.firebase.firestore.auth.User;


import org.w3c.dom.Comment;

import java.util.List;

/**
 * Adapter for displaying comments on an event.
 * @author Noah
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<UserComment> comments;
    private boolean isOrganizer;
    private OnDeleteClickListener listener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView comment;
        private Button deleteCommentButton;

        public ViewHolder(View view, OnDeleteClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View

            name = view.findViewById(R.id.user_comment_name);
            comment = view.findViewById(R.id.user_comment_text);
            deleteCommentButton = view.findViewById(R.id.delete_comment_button);


        }

    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView
     */
    public CommentAdapter(List<UserComment> dataSet, boolean isOrganizer, OnDeleteClickListener listener) {

        this.comments = dataSet;
        this.isOrganizer = isOrganizer;
        this.listener = listener;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.display_comment, viewGroup, false);

        return new ViewHolder(view, listener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        UserComment userComment = comments.get(position);

        // set information to UI
        if(userComment.getIsOrganizer()){
            viewHolder.name.setText(userComment.getUserName() + " (Organizer)");
        } else {
            viewHolder.name.setText(userComment.getUserName());
        }
        viewHolder.comment.setText(userComment.getText());

        // set the delete button to invisible if not organizer
        if (isOrganizer) {
            viewHolder.deleteCommentButton.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteCommentButton.setVisibility(View.INVISIBLE);
        }

        // get the user ID when delete is clicked
        viewHolder.deleteCommentButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(userComment.getCommentID());
            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return comments.size();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String currentCommentID);
    }
}
