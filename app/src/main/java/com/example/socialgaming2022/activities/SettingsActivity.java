package com.example.socialgaming2022.activities;

import android.content.Intent;
import android.icu.util.Currency;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.socialgaming2022.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get the buttons via id
        final Button backToWelcomeButton = findViewById(R.id.backToWelcomeButton);

        // Add click listeners to buttons
        backToWelcomeButton.setOnClickListener(view -> startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class)));

        // Get the dark theme toggle
        SwitchMaterial darkThemeSwitch = findViewById(R.id.darkThemeSwitch);

        // Set the switch to the current nightMode from the shared preferences
        darkThemeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

        // Change night mode everytime the toggle gets clicked
        darkThemeSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (compoundButton.isShown()) {
                if (isChecked && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if (!isChecked && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Display local currency
        Locale locale = Locale.getDefault();
        Currency currency = Currency.getInstance(locale);
        String symbol = currency.getSymbol();
        final TextView currencyText = findViewById(R.id.currencyText);
        currencyText.setText(getString(R.string.currency, symbol));

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the current user and check if the user is logged in
        firebaseUser = firebaseAuth.getCurrentUser();

        // If the user is logged in display settings information
        if (firebaseUser == null)
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
    }
}