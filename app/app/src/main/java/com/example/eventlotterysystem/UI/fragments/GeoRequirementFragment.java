package com.example.eventlotterysystem.UI.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.database.EventRepository;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.example.eventlotterysystem.model.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * Fragment that allows an organizer to enable or disable the geolocation
 * requirement for their event.
 *
 * US-02.02.03: As an organizer I want to enable or disable the geolocation
 * requirement for my event.
 *
 * Acceptance criteria covered:
 *  - Toggle to enable/disable (defaults to disabled).
 *  - When enabled, organizer picks "location included" or "location excluded",
 *    sets a center point and radius.
 *  - Settings are persisted to Firebase.
 *  - When enabled, entrants currently on the waiting list who do not meet the
 *    requirement are removed.
 */
public class GeoRequirementFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    public static final String MODE_INCLUDED = "included";
    public static final String MODE_EXCLUDED = "excluded";

    private String eventId;

    // UI
    private SwitchMaterial geoSwitch;
    private TextView geoStatusText;
    private LinearLayout geoSettingsContainer;
    private RadioGroup geoModeGroup;
    private RadioButton radioIncluded;
    private RadioButton radioExcluded;
    private EditText editLatitude;
    private EditText editLongitude;
    private EditText editRadius;
    private Button useMyLocationButton;
    private Button backButton;
    private Button saveButton;

    private final EventRepository eventRepository = new EventRepository();
    private final EntrantListFirebase entrantListFirebase = new EntrantListFirebase();

    public GeoRequirementFragment() { /* required empty constructor */ }

    /**
     * Create a new instance for the given event.
     *
     * @param eventId the Firestore document ID of the event
     * @return a configured GeoRequirementFragment
     */
    public static GeoRequirementFragment newInstance(String eventId) {
        GeoRequirementFragment fragment = new GeoRequirementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geo_requirement, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        // Bind UI
        geoSwitch = view.findViewById(R.id.geoSwitch);
        geoStatusText = view.findViewById(R.id.geoStatusText);
        geoSettingsContainer = view.findViewById(R.id.geoSettingsContainer);
        geoModeGroup = view.findViewById(R.id.geoModeGroup);
        radioIncluded = view.findViewById(R.id.radioIncluded);
        radioExcluded = view.findViewById(R.id.radioExcluded);
        editLatitude = view.findViewById(R.id.editLatitude);
        editLongitude = view.findViewById(R.id.editLongitude);
        editRadius = view.findViewById(R.id.editRadius);
        useMyLocationButton = view.findViewById(R.id.useMyLocationButton);
        backButton = view.findViewById(R.id.geoBackButton);
        saveButton = view.findViewById(R.id.geoSaveButton);

        // Back button
        backButton.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Toggle visibility of settings when switch is flipped
        geoSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateSettingsVisibility(isChecked));

        // Fill in the organizer's current location as the center point
        useMyLocationButton.setOnClickListener(v -> fillCurrentLocation());

        // Persist to Firebase
        saveButton.setOnClickListener(v -> saveGeoRequirement());

        // Load existing values from Firebase
        loadCurrentSettings();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Show or hide the detailed settings panel and update the status label.
     */
    private void updateSettingsVisibility(boolean enabled) {
        if (enabled) {
            geoSettingsContainer.setVisibility(View.VISIBLE);
            geoStatusText.setText("Enabled — entrants must meet the location requirement to join the waiting list.");
        } else {
            geoSettingsContainer.setVisibility(View.GONE);
            geoStatusText.setText("Disabled — all entrants may join the waiting list.");
        }
    }

    /**
     * Load the current geolocation settings for this event from Firebase and
     * populate the UI controls.
     */
    private void loadCurrentSettings() {
        eventRepository.getEvent(eventId, new EventRepository.SingleEventCallback() {
            @Override
            public void onEventLoaded(Event event) {
                if (!isAdded()) return;

                boolean enabled = event.isGeoRequirementEnabled();
                geoSwitch.setChecked(enabled);
                updateSettingsVisibility(enabled);

                String mode = event.getGeoRequirementMode();
                if (MODE_EXCLUDED.equals(mode)) {
                    radioExcluded.setChecked(true);
                } else {
                    radioIncluded.setChecked(true); // default
                }

                Double lat = event.getGeoRequirementLatitude();
                Double lng = event.getGeoRequirementLongitude();
                Double radius = event.getGeoRequirementRadiusKm();

                if (lat != null) editLatitude.setText(String.valueOf(lat));
                if (lng != null) editLongitude.setText(String.valueOf(lng));
                if (radius != null) editRadius.setText(String.valueOf(radius));
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Failed to load geolocation settings: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populate the latitude and longitude fields with the organizer's current
     * GPS location.
     */
    private void fillCurrentLocation() {
        if (!isAdded()) return;

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(),
                    "Location permission is required", Toast.LENGTH_SHORT).show();
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (!isAdded()) return;
                    if (location != null) {
                        editLatitude.setText(String.valueOf(location.getLatitude()));
                        editLongitude.setText(String.valueOf(location.getLongitude()));
                    } else {
                        client.getLastLocation().addOnSuccessListener(last -> {
                            if (!isAdded()) return;
                            if (last != null) {
                                editLatitude.setText(String.valueOf(last.getLatitude()));
                                editLongitude.setText(String.valueOf(last.getLongitude()));
                            } else {
                                Toast.makeText(getContext(),
                                        "Could not retrieve location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not retrieve location", Toast.LENGTH_SHORT).show());
    }

    /**
     * Validate inputs, persist settings to Firebase, and — when enabling —
     * remove any waiting-list entrants who no longer meet the requirement.
     */
    private void saveGeoRequirement() {
        boolean enabled = geoSwitch.isChecked();

        // If disabling, no location details are required
        if (!enabled) {
            persistAndFinish(false, null, null, null, null);
            return;
        }

        // Validate inputs
        String latStr = editLatitude.getText().toString().trim();
        String lngStr = editLongitude.getText().toString().trim();
        String radStr = editRadius.getText().toString().trim();

        if (latStr.isEmpty() || lngStr.isEmpty() || radStr.isEmpty()) {
            Toast.makeText(getContext(),
                    "Please fill in latitude, longitude, and radius", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat, lng, radius;
        try {
            lat = Double.parseDouble(latStr);
            lng = Double.parseDouble(lngStr);
            radius = Double.parseDouble(radStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Invalid number format in coordinates or radius", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lat < -90 || lat > 90) {
            Toast.makeText(getContext(),
                    "Latitude must be between -90 and 90", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lng < -180 || lng > 180) {
            Toast.makeText(getContext(),
                    "Longitude must be between -180 and 180", Toast.LENGTH_SHORT).show();
            return;
        }
        if (radius <= 0) {
            Toast.makeText(getContext(),
                    "Radius must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        String mode = radioExcluded.isChecked() ? MODE_EXCLUDED : MODE_INCLUDED;
        persistAndFinish(true, mode, lat, lng, radius);
    }

    /**
     * Write settings to Firestore and, when enabling, enforce the requirement
     * against the current waiting list.
     */
    private void persistAndFinish(boolean enabled, String mode,
                                  Double lat, Double lng, Double radiusKm) {
        saveButton.setEnabled(false);

        eventRepository.updateGeoRequirement(
                eventId, enabled, mode, lat, lng, radiusKm,
                new EventRepository.OnDeleteListener() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        if (enabled) {
                            // Enforce the requirement against the current waiting list
                            enforceRequirementOnWaitlist(mode, lat, lng, radiusKm);
                        } else {
                            saveButton.setEnabled(true);
                            Toast.makeText(getContext(),
                                    "Geolocation requirement disabled", Toast.LENGTH_SHORT).show();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        saveButton.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetch all WAITLIST entrants and drop any that do not satisfy the new
     * geolocation requirement.
     *
     * @param mode     "included" or "excluded"
     * @param centerLat center latitude of the zone
     * @param centerLng center longitude of the zone
     * @param radiusKm  radius in kilometres
     */
    private void enforceRequirementOnWaitlist(String mode,
                                              double centerLat, double centerLng,
                                              double radiusKm) {
        entrantListFirebase.getEntrantsByStatus(eventId, EntrantListEntry.STATUS_WAITLIST)
                .addOnSuccessListener(entries -> {
                    if (!isAdded()) return;

                    int[] pending = {entries.size()};
                    if (pending[0] == 0) {
                        finishAfterEnforcement();
                        return;
                    }

                    for (EntrantListEntry entry : entries) {
                        Double entrantLat = entry.getLatitude();
                        Double entrantLng = entry.getLongitude();

                        boolean meetsRequirement = evaluateRequirement(
                                entrantLat, entrantLng,
                                centerLat, centerLng, radiusKm, mode);

                        if (!meetsRequirement) {
                            // Drop this entrant from the waiting list
                            entrantListFirebase.updateStatus(
                                    eventId,
                                    entry.getEntrantId(),
                                    EntrantListEntry.STATUS_CANCELLED_OR_REJECTED
                            ).addOnCompleteListener(task -> {
                                pending[0]--;
                                if (pending[0] == 0 && isAdded()) {
                                    finishAfterEnforcement();
                                }
                            });
                        } else {
                            pending[0]--;
                            if (pending[0] == 0 && isAdded()) {
                                finishAfterEnforcement();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    // Settings saved but we couldn't enforce — warn and go back
                    saveButton.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Settings saved. Could not check waiting list: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                });
    }

    private void finishAfterEnforcement() {
        saveButton.setEnabled(true);
        Toast.makeText(getContext(),
                "Geolocation requirement saved and waiting list updated",
                Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    /**
     * Decide whether an entrant at (entrantLat, entrantLng) satisfies the
     * geolocation requirement.
     *
     * <p>If the entrant has no stored location their coordinates are null; they
     * are treated as <em>not meeting</em> the requirement (safe default).</p>
     *
     * @param entrantLat latitude stored in the entrant's waiting-list entry
     * @param entrantLng longitude stored in the entrant's waiting-list entry
     * @param centerLat  center latitude of the requirement zone
     * @param centerLng  center longitude of the requirement zone
     * @param radiusKm   radius of the zone in kilometres
     * @param mode       "included" (entrant must be inside) or
     *                   "excluded" (entrant must be outside)
     * @return true if the entrant satisfies the requirement
     */
    public static boolean evaluateRequirement(Double entrantLat, Double entrantLng,
                                              double centerLat, double centerLng,
                                              double radiusKm, String mode) {
        if (entrantLat == null || entrantLng == null) {
            return false; // no location data — cannot verify, reject
        }

        double distanceKm = haversineDistanceKm(entrantLat, entrantLng, centerLat, centerLng);
        boolean withinZone = distanceKm <= radiusKm;

        if (MODE_INCLUDED.equals(mode)) {
            return withinZone;       // must be inside the zone
        } else {
            return !withinZone;      // must be outside the zone
        }
    }

    /**
     * Haversine formula: great-circle distance between two points on Earth.
     *
     * @return distance in kilometres
     */
    private static double haversineDistanceKm(double lat1, double lon1,
                                              double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}