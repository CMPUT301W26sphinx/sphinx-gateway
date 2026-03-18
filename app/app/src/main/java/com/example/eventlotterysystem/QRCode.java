package com.example.eventlotterysystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Collections;

/** Based off official CameraX-MLkit dev<p>
 * https://github.com/android/camera-samples/tree/main/CameraX-MLKit <p>
 *     Supposed to be one which creates QR, but now is seperated to fragments
 * @author Bryan Jonathan
 */

public class QRCode extends AppCompatActivity {
    private static final String TAG = "CameraX-MLKit";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    public static final String QR_RESULT = "qr_result";

    private boolean resultDelivered = false; // camera sends multiple frames to MLkit, and if MLkit is processing, it will turn this boolean

    //references for UI
    private PreviewView previewView;
    private BarcodeScanner barcodeScanner;


    // Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcodescanner);

        previewView = findViewById(R.id.previewView);

        if (hasCameraPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    // Camera Permission handling
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    // Camera Setup
    private void startCamera() {
        // Build the barcode scanner (QR codes only)
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        LifecycleCameraController cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);

        cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(this),
                new MlKitAnalyzer(
                        Collections.singletonList(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                        ContextCompat.getMainExecutor(this),
                        result -> {
                            if (resultDelivered) return;

                            java.util.List<Barcode> barcodes =
                                    result.getValue(barcodeScanner);

                            if (barcodes == null || barcodes.isEmpty()) return;

                            String raw = barcodes.get(0).getRawValue();
                            if (raw == null) return;

                            Log.d(TAG, "QR scanned: " + raw);
                            resultDelivered = true;
                            deliverResult(raw);
                        }
                )
        );
        previewView.setController(cameraController);
    }
    private void deliverResult(String result) {
        Intent data = new Intent();
        data.putExtra(QR_RESULT, result);
        setResult(RESULT_OK, data);
        finish();
    }
}
