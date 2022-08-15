package com.example.socialgaming2022.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.socialgaming2022.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
            if(compoundButton.isShown()) {
                if (isChecked && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else if (!isChecked && AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Get the language spinner
        Spinner languageSpinner = findViewById(R.id.languageSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        languageSpinner.setAdapter(adapter);

        // Get the selected language
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String itemAtPosition = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "itemAtPosition: " + itemAtPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the current user and check if the user is logged in
        firebaseUser = firebaseAuth.getCurrentUser();

        // If the user is logged in display settings information
        if(firebaseUser == null)
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
    }
}