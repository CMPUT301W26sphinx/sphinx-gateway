package com.example.eventlotterysystem.UI.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying a list of categories with checkboxes.
 * Supports filtering by search query and maintains selected state.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> allCategories;          // all categories (unfiltered)
    private List<String> filteredCategories;     // categories after search filter
    private Set<String> selectedCategories;      // set of currently selected categories

    public CategoryAdapter(List<String> allCategories, Set<String> selectedCategories) {
        this.allCategories = allCategories;
        this.filteredCategories = new ArrayList<>(allCategories);
        this.selectedCategories = selectedCategories;
    }

    /**
     * Filters the category list based on a search query.
     * @param query the search string (case‑insensitive)
     */
    public void filter(String query) {
        filteredCategories.clear();
        if (query.isEmpty()) {
            filteredCategories.addAll(allCategories);
        } else {
            String lower = query.toLowerCase();
            for (String cat : allCategories) {
                if (cat.toLowerCase().contains(lower)) {
                    filteredCategories.add(cat);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = filteredCategories.get(position);
        holder.checkBox.setText(category);
        holder.checkBox.setChecked(selectedCategories.contains(category));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategories.add(category);
            } else {
                selectedCategories.remove(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredCategories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.category_checkbox);
        }
    }
}