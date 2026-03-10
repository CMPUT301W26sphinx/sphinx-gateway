package com.example.eventlotterysystem.UI.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.model.QRScannerCore;

public class QRCodeFragment extends Fragment {
    private PreviewView previewView;
    private LifecycleCameraController cameraController;
    private QRScannerCore scannerCore;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startScanner();
                } else {
                    Toast.makeText(requireContext(),
                            "Camera permission is required to scan QR codes.",
                            Toast.LENGTH_LONG).show();
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qrcode_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);

        if (hasCameraPermission()) {
            startScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (scannerCore != null && cameraController != null) {
            scannerCore.detach(cameraController);
        }
    }


    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }



    private void startScanner() {
        cameraController = new LifecycleCameraController(requireContext());
        cameraController.bindToLifecycle(getViewLifecycleOwner());
        previewView.setController(cameraController);

        scannerCore = new QRScannerCore(requireContext(), this::onQRScanned);
        scannerCore.attach(cameraController, getViewLifecycleOwner());
    }


    private void onQRScanned(String result) {
        // TODO: hand it to EventList, or if it is wrong QR code, then scannerCore.reset()
        Toast.makeText(requireContext(), "Scanned: " + result, Toast.LENGTH_LONG).show();
    }
}
