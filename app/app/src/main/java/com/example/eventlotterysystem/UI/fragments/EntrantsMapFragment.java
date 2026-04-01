package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class EntrantsMapFragment extends Fragment {

    private MapView mapView;
    private String eventId;

    public EntrantsMapFragment() {}

    public static EntrantsMapFragment newInstance(String eventId) {
        EntrantsMapFragment fragment = new EntrantsMapFragment();
        Bundle args = new Bundle();
        args.putString("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // REQUIRED for osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map);
        mapView.setMultiTouchControls(true);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
        }

        loadEntrants();

        return view;
    }

    /**
     * Load entrants from Firestore and place markers
     */
    private void loadEntrants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("EntrantList")
                .get()
                .addOnSuccessListener(query -> {

                    mapView.getOverlays().clear();

                    org.osmdroid.util.GeoPoint firstLocation = null;

                    for (var doc : query.getDocuments()) {

                        GeoPoint geoPoint = doc.getGeoPoint("location");

                        if (geoPoint != null) {

                            org.osmdroid.util.GeoPoint position =
                                    new org.osmdroid.util.GeoPoint(
                                            geoPoint.getLatitude(),
                                            geoPoint.getLongitude()
                                    );

                            Marker marker = new Marker(mapView);
                            marker.setPosition(position);
                            marker.setTitle("Entrant");

                            mapView.getOverlays().add(marker);

                            if (firstLocation == null) {
                                firstLocation = position;
                            }
                        }
                    }

                    // Center map
                    if (firstLocation != null) {
                        mapView.getController().setZoom(12.0);
                        mapView.getController().setCenter(firstLocation);
                    } else {
                        // fallback (Edmonton)
                        org.osmdroid.util.GeoPoint fallback =
                                new org.osmdroid.util.GeoPoint(53.5461, -113.4938);

                        mapView.getController().setZoom(12.0);
                        mapView.getController().setCenter(fallback);
                    }

                    mapView.invalidate();
                });
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