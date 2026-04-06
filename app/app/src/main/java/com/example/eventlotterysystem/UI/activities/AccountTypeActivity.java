package com.example.eventlotterysystem.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.MainActivity;
import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminHomeActivity;

/**
 * This activity is used to select the account type for the user.
 * @author Hassan
 */
public class AccountTypeActivity extends AppCompatActivity {

    private AutoCompleteTextView dropdownAccountType;
    private Button btnNext;

    // Admin Android ID
    private static final String AUTHORIZED_DEVICE_ID = "85e1384542b9313d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);



        // Get the current device's Android ID
        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // If not your device, skip directly to entrant flow
        if (!AUTHORIZED_DEVICE_ID.equals(currentDeviceId)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("show_terms_popup", true);
            startActivity(intent);
            finish();
            return;
        }


        setContentView(R.layout.activity_account_type);

        dropdownAccountType = findViewById(R.id.dropdown_account_type);
        btnNext = findViewById(R.id.btn_next);

        String[] accountTypes = {"Admin", "Entrant"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                accountTypes
        );

        dropdownAccountType.setAdapter(adapter);
        dropdownAccountType.setText(accountTypes[0], false);

        btnNext.setOnClickListener(v -> {
            String selectedType = dropdownAccountType.getText().toString().trim();

            if (selectedType.equals("Admin")) {
                startActivity(new Intent(this, AdminHomeActivity.class));
            } else if (selectedType.equals("Entrant")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("show_terms_popup", true);
                startActivity(intent);
            }

            finish();
        });
    }
}