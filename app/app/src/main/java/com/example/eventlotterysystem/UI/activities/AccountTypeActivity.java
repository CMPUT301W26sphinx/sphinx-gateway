package com.example.eventlotterysystem.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlotterysystem.R;
import com.example.eventlotterysystem.UI.activities.admin.AdminHomeActivity;

public class AccountTypeActivity extends AppCompatActivity {

    private AutoCompleteTextView dropdownAccountType;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                startActivity(new Intent(this, TermsActivity.class));
            }

            finish();
        });
    }
}