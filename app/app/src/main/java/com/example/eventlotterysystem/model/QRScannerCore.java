package com.example.eventlotterysystem.model;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Collections;
import java.util.List;

/**
 * QRScannerCore — handles all QR scanning logic using CameraX and ML Kit
 *
 */
public class QRScannerCore {

    public interface OnQRScannedListener {
        void onScanned(String result);
    }

    private final Context context;
    private final OnQRScannedListener listener;
    private final BarcodeScanner barcodeScanner;
    private boolean resultDelivered = false;

    /**
     * QRScannerCore — handles all QR scanning logic using CameraX and ML Kit
     * <a href="https://github.com/android/camera-samples/tree/main/CameraX-MLKit">...</a>
     * @author Bryan Jonathan
     */
    public QRScannerCore(Context context, OnQRScannedListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }
    public void attach(LifecycleCameraController cameraController, LifecycleOwner lifecycleOwner) {
        resultDelivered = false;

        cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                new MlKitAnalyzer(
                        Collections.singletonList(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                        ContextCompat.getMainExecutor(context),
                        (MlKitAnalyzer.Result result) -> {
                            if (resultDelivered) return;

                            List<Barcode> barcodes = result.getValue(barcodeScanner);
                            if (barcodes == null || barcodes.isEmpty()) return;

                            String raw = barcodes.get(0).getRawValue();
                            if (raw == null || raw.isEmpty()) return;

                            Log.d("QRScannerCore", "QR scanned: " + raw);
                            resultDelivered = true;
                            listener.onScanned(raw);
                        }
                )
        );
    }
    public void detach(LifecycleCameraController cameraController) {
        cameraController.clearImageAnalysisAnalyzer();
        barcodeScanner.close();
    }
    public void reset() {
        resultDelivered = false;
    }
}
