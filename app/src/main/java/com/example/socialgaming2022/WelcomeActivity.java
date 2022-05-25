package com.example.socialgaming2022;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get buttons
        Button mapButton = findViewById(R.id.mapButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        mapButton.setOnClickListener(view -> switchToMapActivity());
        logoutButton.setOnClickListener(view -> logoutUser());
    }

    private void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(WelcomeActivity.this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", (dialogInterface, i) -> {
            firebaseAuth.signOut();
            dialogInterface.dismiss();
            switchToMainActivity();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void switchToMapActivity() {
        Intent intent = new Intent(WelcomeActivity.this, OSMActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        logoutUser();
    }
}