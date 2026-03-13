package com.example.eventlotterysystem.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminHomeActivity;

public class AccountTypeActivity extends AppCompatActivity {

    private Spinner spinnerAccountType;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        spinnerAccountType = findViewById(R.id.spinner_account_type);
        btnNext = findViewById(R.id.btn_next);

        String[] accountTypes = {"Admin", "Entrant"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            String selectedType = spinnerAccountType.getSelectedItem().toString();

            if (selectedType.equals("Admin")) {
                // Directly open admin home (no login)
                startActivity(new Intent(this, AdminHomeActivity.class));
            } else if (selectedType.equals("Entrant")) {
                startActivity(new Intent(this, TermsActivity.class));
            }

            finish();
        });
    }
}