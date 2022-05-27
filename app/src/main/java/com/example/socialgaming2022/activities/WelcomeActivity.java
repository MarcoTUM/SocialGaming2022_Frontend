package com.example.socialgaming2022.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.socialgaming2022.R;
import com.example.socialgaming2022.helper.PlayerVolleyHelper;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

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
        Button mapButton = findViewById(R.id.mapButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        mapButton.setOnClickListener(view -> switchToMapActivity());
        logoutButton.setOnClickListener(view -> logoutUser());
    }

    private void addUserNicknameToWelcomeText() {
        // Get EditText
        TextView welcomeText = findViewById(R.id.welcomeText);

        PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
        playerVolleyHelper.getPlayerByFirebaseUID(firebaseAuth.getCurrentUser().getUid(),
                response -> {
                    // Add the nickname to welcome text
                    try {
                        welcomeText.setText(welcomeText.getText() + " " + response.getString("nickname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.w(TAG, "addUserNicknameToWelcomeText: Did not get nickname from backend!", error)
        );
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