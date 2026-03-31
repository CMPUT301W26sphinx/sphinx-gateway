package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventlotterysystem.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class EntrantsMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString("event_id");
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MAP_DEBUG", "Map is ready");
        LatLng test = new LatLng(53.5461, -113.4938);
        mMap.addMarker(new MarkerOptions().position(test).title("TEST"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 12f));
        loadEntrants();
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

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean hasMarkers = false;

                    for (var doc : query.getDocuments()) {

                        GeoPoint geoPoint = doc.getGeoPoint("location");

                        if (geoPoint != null) {

                            LatLng position = new LatLng(
                                    geoPoint.getLatitude(),
                                    geoPoint.getLongitude()
                            );

                            // 🔥 FORCE visible marker (bright color)
                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title("Entrant")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            builder.include(position);
                            hasMarkers = true;
                        }
                    }

                    if (hasMarkers) {
                        LatLngBounds bounds = builder.build();

                        mMap.setOnMapLoadedCallback(() -> {
                            mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 200)
                            );
                        });
                    } else {
                        // fallback test
                        LatLng test = new LatLng(53.5461, -113.4938);

                        mMap.addMarker(new MarkerOptions().position(test).title("TEST"));

                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(test, 12f)
                        );
                    }
                });
    }
}