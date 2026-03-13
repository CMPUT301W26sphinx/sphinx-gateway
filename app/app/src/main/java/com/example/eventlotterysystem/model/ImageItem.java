package com.example.eventlotterysystem.model;

import java.io.Serializable;

public class ImageItem implements Serializable {
    private String id;           // Firestore document ID (event ID or image ID)
    private String title;         // event name or image title
    private String description;   // event description or image caption
    private String imageUrl;      // placeholder URL or actual image URL
    private String uploaderName;  // e.g., "System" or uploader's name

    // Required no-arg constructor for Firestore (if used later)
    public ImageItem() {}

    public ImageItem(String id, String title, String description, String imageUrl, String uploaderName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.uploaderName = uploaderName;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
}