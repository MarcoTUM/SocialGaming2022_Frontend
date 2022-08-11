package com.example.socialgaming2022.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.socialgaming2022.R;
import com.example.socialgaming2022.helper.PlayerVolleyHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get the buttons via id
        final Button backToWelcomeButton = findViewById(R.id.backToWelcomeButton);
        final Button deleteButton = findViewById(R.id.deleteButton);

        // Add click listeners to buttons
        backToWelcomeButton.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class)));
        deleteButton.setOnClickListener(view -> deleteUserAccount());

        // Get the dark theme toggle
        final SwitchMaterial darkThemeSwitch = findViewById(R.id.darkThemeSwitch);
        darkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the current user and check if the user is logged in
        firebaseUser = firebaseAuth.getCurrentUser();

        // If the user is logged in display profile information
        if(firebaseUser != null)
            displayProfileInformation();
        // Otherwise send the user back to login
        else
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
    }

    private void displayProfileInformation() {
        // Get text via id
        final TextView displayName = findViewById(R.id.displayName);
        final TextView nickname = findViewById(R.id.nickname);
        final TextView gamesPlayed = findViewById(R.id.gamesPlayed);
        final TextView gamesWon = findViewById(R.id.gamesWon);

        // Get display name from Firebase
        displayName.setText(getString(R.string.display_name, firebaseUser.getDisplayName()));

        // Get other information from MongoDB
        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseUser.getUid(),
        response -> {
            try {
                // Set text views to player data
                nickname.setText(getString(R.string.nickname, response.getString("nickname")));
                gamesPlayed.setText(getString(R.string.games_played, response.getInt("gamesPlayed")));
                gamesWon.setText(getString(R.string.games_won, response.getInt("gamesWon")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
                    Log.w(TAG, "displayProfileInformation: ", error);
                    Toast.makeText(ProfileActivity.this, "Could not receive profile information from database!", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteUserAccount() {
        AlertDialog alertDialog = new AlertDialog.Builder(ProfileActivity.this).create();
        alertDialog.setTitle("Delete account!");
        alertDialog.setMessage("Do you really want delete your account?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", (dialog, which) -> {
            // Delete account in MongoDB
            PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
            playerVolleyHelper.deleteUserByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                    response -> {
                        // Delete the FirebaseAuth account
                        firebaseAuth.getCurrentUser().delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Firebase User account deleted.");
                                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                    },
                    error -> {
                        Log.w(TAG, "deleteUserAccount: ", error);
                        Toast.makeText(ProfileActivity.this, "Deleting user account failed.", Toast.LENGTH_SHORT).show();
                    });
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        alertDialog.show();
    }
}