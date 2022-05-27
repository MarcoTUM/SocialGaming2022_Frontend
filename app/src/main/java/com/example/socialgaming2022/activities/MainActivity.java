package com.example.socialgaming2022.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialgaming2022.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call superclass onCreate method
        super.onCreate(savedInstanceState);

        // Set the activity content from a layout source
        setContentView(R.layout.activity_main);

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the current user from firebase
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // If the user is not logged in
        if(firebaseUser == null) {
            // Get button via id
            Button loginButton = findViewById(R.id.loginButton);
            Button registerButton = findViewById(R.id.registerButton);

            // Get EditText field values
            final EditText email = findViewById(R.id.email);
            final EditText password = findViewById(R.id.password);

            // Send login information to firebase
            loginButton.setOnClickListener(view -> loginUser(email.getText().toString(), password.getText().toString()));

            // Switch to the register activity
            registerButton.setOnClickListener(view -> switchToRegisterActivity());
        } else {
            // Switch to the welcome activity
            switchToWelcomeActivity();
        }
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if(task.isSuccessful()) {
                    Log.d(TAG, "loginUser: User successfully logged in!");
                    switchToWelcomeActivity();
                } else {
                    Log.d(TAG, "loginUser: Failed to login!", task.getException());
                    Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                }
        });
    }

    private void switchToWelcomeActivity() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
    }

    private void switchToRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}