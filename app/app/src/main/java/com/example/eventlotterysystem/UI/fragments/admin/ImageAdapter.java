package com.example.eventlotterysystem.UI.fragments.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.Event;
import com.example.eventlotterysystem.model.ImageItem;
import com.example.eventlotterysystem.utils.ImageHelper;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<ImageItem> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ImageItem image);
    }

    public ImageAdapter(List<ImageItem> images, OnItemClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem image = images.get(position);
        holder.title.setText(image.getTitle());

        // Create a dummy Event object with the image data
        Event dummyEvent = new Event();
        dummyEvent.setEventId(image.getId());
        dummyEvent.setImageData(image.getImageData());

        // Load image using helper
        ImageHelper.loadEventImage(holder.thumbnail, dummyEvent);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(image));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.image_thumbnail);
            title = itemView.findViewById(R.id.image_title);
        }
    }
}