package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class OrganizerEventMapFragment extends Fragment {

    private MapView mapView;
    private String eventId;

    public OrganizerEventMapFragment() {}

    public static OrganizerEventMapFragment newInstance(String eventId) {
        OrganizerEventMapFragment fragment = new OrganizerEventMapFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_org_event_map, container, false);

        // Initialize map
        mapView = view.findViewById(R.id.mapView);

        // OSMDroid config
        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Map settings
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);

        // ✅ Optional but recommended (prevents over/under zoom issues)
        mapView.setMinZoomLevel(4.0);
        mapView.setMaxZoomLevel(19.0);

        // Get eventId
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Load markers
        loadEntrants();

        return view;
    }


    private void loadEntrants() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("EntrantList")
                .get()
                .addOnSuccessListener(query -> {

                    mapView.getOverlays().clear();

                    if (query.isEmpty()) {
                        setDefaultLocation();
                        return;
                    }

                    int total = query.size();
                    final int[] loaded = {0};

                    Map<String, Integer> locationCounts = new HashMap<>();
                    Map<String, org.osmdroid.util.GeoPoint> locationCoords = new HashMap<>();

                    final double[] minLat = {Double.MAX_VALUE};
                    final double[] maxLat = {-Double.MAX_VALUE};
                    final double[] minLng = {Double.MAX_VALUE};
                    final double[] maxLng = {-Double.MAX_VALUE};

                    for (var doc : query.getDocuments()) {

                        Double entryLat = doc.getDouble("latitude");
                        Double entryLng = doc.getDouble("longitude");

                        if (entryLat != null && entryLng != null && !(entryLat == 0 && entryLng == 0)) {
                            loaded[0]++;
                            addLocationToMaps(locationCounts, locationCoords, minLat, maxLat, minLng, maxLng, entryLat, entryLng);

                            if (loaded[0] == total) {
                                renderGroupedMarkers(locationCounts, locationCoords, minLat[0], maxLat[0], minLng[0], maxLng[0]);
                            }
                            continue;
                        }

                        String userId = doc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    loaded[0]++;

                                    Double lat = userDoc.getDouble("latitude");
                                    Double lng = userDoc.getDouble("longitude");

                                    Log.d("OrganizerEventMap", "Loaded location for " + userId + ": " + lat + ", " + lng);

                                    if (lat != null && lng != null && !(lat == 0 && lng == 0)) {
                                        addLocationToMaps(locationCounts, locationCoords, minLat, maxLat, minLng, maxLng, lat, lng);
                                    }

                                    if (loaded[0] == total) {
                                        renderGroupedMarkers(locationCounts, locationCoords, minLat[0], maxLat[0], minLng[0], maxLng[0]);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loaded[0]++;
                                    if (loaded[0] == total) {
                                        renderGroupedMarkers(locationCounts, locationCoords, minLat[0], maxLat[0], minLng[0], maxLng[0]);
                                    }
                                });
                    }
                });
    }

    private void addLocationToMaps(Map<String, Integer> locationCounts,
                                   Map<String, org.osmdroid.util.GeoPoint> locationCoords,
                                   double[] minLat,
                                   double[] maxLat,
                                   double[] minLng,
                                   double[] maxLng,
                                   double lat,
                                   double lng) {

        String key = lat + "," + lng;
        locationCounts.put(key, locationCounts.getOrDefault(key, 0) + 1);
        locationCoords.put(key, new org.osmdroid.util.GeoPoint(lat, lng));

        minLat[0] = Math.min(minLat[0], lat);
        maxLat[0] = Math.max(maxLat[0], lat);
        minLng[0] = Math.min(minLng[0], lng);
        maxLng[0] = Math.max(maxLng[0], lng);
    }

    private void renderGroupedMarkers(Map<String, Integer> locationCounts,
                                      Map<String, org.osmdroid.util.GeoPoint> locationCoords,
                                      double minLat,
                                      double maxLat,
                                      double minLng,
                                      double maxLng) {

        for (String key : locationCounts.keySet()) {
            org.osmdroid.util.GeoPoint point = locationCoords.get(key);
            int count = locationCounts.get(key);
            addMarker(point.getLatitude(), point.getLongitude(), "Entrants: " + count);
        }

        mapView.post(() -> {
            if (minLat != Double.MAX_VALUE) {
                if (minLat == maxLat && minLng == maxLng) {
                    org.osmdroid.util.GeoPoint point = new org.osmdroid.util.GeoPoint(minLat, minLng);
                    mapView.getController().setZoom(17.0);
                    mapView.getController().animateTo(point);
                } else {
                    org.osmdroid.util.BoundingBox box =
                            new org.osmdroid.util.BoundingBox(maxLat, maxLng, minLat, minLng);
                    mapView.zoomToBoundingBox(box, true, 120);
                    mapView.getController().setZoom(Math.min(mapView.getZoomLevelDouble(), 18.0));
                }
            } else {
                setDefaultLocation();
            }
            mapView.invalidate();
        });
    }

    private void addMarker(double lat, double lng, String title) {

        org.osmdroid.util.GeoPoint position =
                new org.osmdroid.util.GeoPoint(lat, lng);

        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title != null ? title : "Entrant");

        mapView.getOverlays().add(marker);
    }

    private void setDefaultLocation() {
        org.osmdroid.util.GeoPoint fallback =
                new org.osmdroid.util.GeoPoint(53.5461, -113.4938);

        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(fallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}