package com.example.socialgaming2022.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialgaming2022.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        addUserNicknameToWelcomeText();

        // Get buttons
        Button profileButton = findViewById(R.id.profileButton);
        Button friendsButton = findViewById(R.id.friendsButton);
        Button mapButton = findViewById(R.id.mapButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        profileButton.setOnClickListener(view -> switchToProfileActivity());
        friendsButton.setOnClickListener(view -> switchToFriendsActivity());
        mapButton.setOnClickListener(view -> switchToMapActivity());
        settingsButton.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));
        logoutButton.setOnClickListener(view -> logoutUser());
    }

    private void addUserNicknameToWelcomeText() {
        // Get EditText
        TextView welcomeText = findViewById(R.id.welcomeText);

        // Get current Firebase user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // If there is currently no user logged in switch back to MainActivity
        if (currentUser == null) {
            switchToLoginActivity();
            return;
        }

        // Add nickname to welcome text
        welcomeText.setText(getString(R.string.welcome, currentUser.getDisplayName()));
    }

    private void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(WelcomeActivity.this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Do you want to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", (dialogInterface, i) -> {
            firebaseAuth.signOut();
            dialogInterface.dismiss();
            switchToLoginActivity();
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void switchToProfileActivity() {
        Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void switchToFriendsActivity() {
        Intent intent = new Intent(WelcomeActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
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