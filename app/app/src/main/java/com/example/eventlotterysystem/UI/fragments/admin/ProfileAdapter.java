package com.example.eventlotterysystem.UI.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.profiles.UserProfile; // adjust import if needed

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<UserProfile> profiles;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserProfile profile);
    }

    public ProfileAdapter(List<UserProfile> profiles, OnItemClickListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        UserProfile profile = profiles.get(position);


        String firstName = profile.getFirstName();
        String lastName = profile.getLastName();
        String fullName;
        if (firstName != null && !firstName.isEmpty()) {
            fullName = firstName;
            if (lastName != null && !lastName.isEmpty()) {
                fullName += " " + lastName;
            }
        } else {
            fullName = "No name";
        }

        holder.name.setText(fullName);
        holder.email.setText(profile.getEmail() != null ? profile.getEmail() : "No email");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(profile));
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            email = itemView.findViewById(R.id.profile_email);
        }
    }
}