package com.example.eventlotterysystem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

// Uses MLKit Google LLC
public class QRCode extends AppCompatActivity {
    private void scanBarcodes (InputImage image){
        // starts the barcode
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE)
                        .build();
    }
}
