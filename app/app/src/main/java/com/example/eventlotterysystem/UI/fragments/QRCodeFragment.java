package com.example.eventlotterysystem.UI.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlotterysystem.R;

/**
 * QRCodeFragment — the UI half of the QR scanner.
 *
 * This is the fragment already registered in your BottomNavigationView.
 * It owns the PreviewView and CameraController, and delegates all
 * scanning logic to QRScannerCore.
 *
 * When a QR code is scanned, onQRScanned(String) is called — override
 * or extend this to handle the result however you need.
 *
 * In MainActivity you already have:
 *   QRCodeFragment qrCodeFragment = new QRCodeFragment();
 *   ...
 *   } else if (id == R.id.QRScan) {
 *       setCurrentFragment(qrCodeFragment);
 *   }
 * No other changes to MainActivity are needed.
 */
public class QRCodeFragment extends Fragment {
    /**
     * TO DO: Create Event List Fragment!!
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qrcode_main, container, false);
    }
}
