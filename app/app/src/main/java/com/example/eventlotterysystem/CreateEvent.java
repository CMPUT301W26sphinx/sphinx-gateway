package com.example.eventlotterysystem;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class CreateEvent extends AppCompatActivity {
    private FirebaseFirestore db;

    private EditText nameInput, descInput, timeInput, placeInput, startRegInput, endRegInput, maxInput;

    private Button saveButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.eventName);
        descInput = findViewById(R.id.eventDescription);
        timeInput = findViewById(R.id.eventTime);
        placeInput = findViewById(R.id.eventPlace);
        startRegInput = findViewById(R.id.regStart);
        endRegInput = findViewById(R.id.regEnd);
        maxInput = findViewById(R.id.maxEntrants);
        saveButton = findViewById(R.id.saveEventButton);

        saveButton.setOnClickListener(v ->
                createEvent());
    }
    private void createEvent() {
        String eventId = db.collection("events").document().getId();
        String name = nameInput.getText().toString();
        String description = descInput.getText().toString();
        String time = timeInput.getText().toString();
        String place = placeInput.getText().toString();
        String start = startRegInput.getText().toString();
        String end = endRegInput.getText().toString();
        Integer maxEntrants = null;
        if (!maxInput.getText().toString().isEmpty()) {
            maxEntrants = Integer.parseInt(maxInput.getText().toString());
        }
        List<String> period = Arrays.asList(
                start.isEmpty() ? null : start,
                end.isEmpty() ? null : end
        );

        Bitmap qrBitmap = generateQRCode(eventId);

        if (qrBitmap == null) {
            Toast.makeText(this,
                    "QR Generation Failed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(
                eventId,
                name,
                description,
                time,
                place,
                null,
                null,
                period,
                "currentUser",
                maxEntrants
        );

        uploadEvent(eventId, qrBitmap, event);
    }

    private Bitmap generateQRCode(String eventId) {
        try {
            String qrData = "lotteryapp://event/" + eventId;

            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void uploadEvent(String eventId, Bitmap bitmap, Event event) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("qrcodes/" + eventId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        storageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String qrUrl = uri.toString();
                                event.setQrUrl(qrUrl);
                                db.collection("events")
                                        .document(eventId)
                                        .set(event)
                                        .addOnSuccessListener(unused ->
                                                Toast.makeText(this,
                                                        "Event Created Successfully",
                                                        Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this,
                                                        "Firestore Save Failed",
                                                        Toast.LENGTH_SHORT).show()
                                        );
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "QR Upload Failed",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
