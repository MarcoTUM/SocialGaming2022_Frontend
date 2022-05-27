package com.example.socialgaming2022.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.socialgaming2022.R;
import com.example.socialgaming2022.helper.PlayerVolleyHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        final EditText nickname = findViewById(R.id.nickname);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);

        // Switch to the login activity
        backToLoginButton.setOnClickListener(view -> switchToMainActivity());

        // Register a new user once the register button has been clicked
        registerButton.setOnClickListener(view -> registerUser(email.getText().toString(), password.getText().toString(), nickname.getText().toString()));
    }

    private void registerUser(String email, String password, String nickname) {
        // Register the user with firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()) {
                Log.d(TAG, "registerUser: User successfully registered in firebase!");
                registerUserInSpringBackend(firebaseAuth.getCurrentUser().getUid(), nickname);
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "registerUser: Failed to register user!", task.getException());
                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUserInSpringBackend(String firebaseUID, String nickname) {
        try {
            // Convert user information to JSON
            JSONObject playerJSON = new JSONObject();
            playerJSON.put("firebaseUID", firebaseUID);
            playerJSON.put("nickname", nickname);
            playerJSON.put("friendsFirebaseUIDs", new JSONArray());

            PlayerVolleyHelper playerVolleyHelper = new PlayerVolleyHelper(this);
            playerVolleyHelper.postRegisterNewUser(playerJSON,
                    response -> {
                        Log.d(TAG, "registerUserInSpringBackend: User successfully registered in Spring backend!");
                        switchToWelcomeActivity();
                    },
                    error -> {
                        Log.d(TAG, "registerUserInSpringBackend: Failed to register user in Spring backend!", error);

                        // Delete the Firebase account
                        firebaseAuth.getCurrentUser().delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Log.d(TAG, "Firebase user account deleted, after registerUserInSpringBackend failed!");
                                        } else {
                                            Log.d(TAG, "Firebase user not account deleted, after registerUserInSpringBackend failed!", task.getException());
                                            Log.d(TAG, "Additional steps required!");
                                            // Additional steps required
                                        }
                                    }
                                });
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void switchToWelcomeActivity() {
        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
        startActivity(intent);
    }
}