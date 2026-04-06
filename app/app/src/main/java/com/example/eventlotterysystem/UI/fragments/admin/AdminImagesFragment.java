package com.example.eventlotterysystem.UI.fragments.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminImageDetailActivity;
import com.example.eventlotterysystem.database.EventImageRepository;
import com.example.eventlotterysystem.database.ImageRepository;
import com.example.eventlotterysystem.model.ImageItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is used to show images in Admin view
 * @author Hassan
 */

public class AdminImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<ImageItem> imageList = new ArrayList<>();
    private ImageRepository repository;
    private TextView emptyState;
    private ProgressBar progressBar;

    public AdminImagesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_images, container, false);

        recyclerView = view.findViewById(R.id.images_recycler);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        adapter = new ImageAdapter(imageList, this::openImageDetail);
        recyclerView.setAdapter(adapter);

        repository = new EventImageRepository(); // Switch implementation here later
        loadImages();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadImages(); // refresh after deletion
    }

    private void loadImages() {
        showLoading(true);
        repository.getAllImages(new ImageRepository.ImagesCallback() {
            @Override
            public void onImagesLoaded(List<ImageItem> images) {
                showLoading(false);
                if (getActivity() == null) return;

                imageList.clear();
                imageList.addAll(images);
                adapter.notifyDataSetChanged();

                if (imageList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : recyclerView.getVisibility());
        emptyState.setVisibility(show ? View.GONE : emptyState.getVisibility());
    }

    private void openImageDetail(ImageItem image) {
        Intent intent = new Intent(getActivity(), AdminImageDetailActivity.class);
        intent.putExtra("image", image);
        startActivity(intent);
    }
}