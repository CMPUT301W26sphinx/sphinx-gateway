package com.example.eventlotterysystem.database;

import com.example.eventlotterysystem.model.ImageItem;
import java.util.List;

public interface ImageRepository {
    void getAllImages(ImagesCallback callback);
    void deleteImage(String imageId, DeleteCallback callback);

    interface ImagesCallback {
        void onImagesLoaded(List<ImageItem> images);
        void onError(Exception e);
    }

    interface DeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }
}