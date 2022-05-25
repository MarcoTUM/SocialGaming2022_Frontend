package com.example.socialgaming2022;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get button via id
        Button backToLoginButton = findViewById(R.id.backToLoginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Get a firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the user information from the EditText fields via id
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);

        // Switch to the login activity
        backToLoginButton.setOnClickListener(view -> switchToMainActivity());

        // Register a new user once the register button has been clicked
        registerButton.setOnClickListener(view -> registerUser(email.getText().toString(), password.getText().toString()));
    }

    private void registerUser(String email, String password) {
        // Register the user with firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()) {
                Log.d(TAG, "registerUser: User successfully registered!");
            } else {
                Log.d(TAG, "registerUser: Failed to register user!", task.getException());
                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }
}