package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Check if the user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, redirect to the main activity
            startActivity(new Intent(LoginActivity.this, SkeletonActivity.class));
            finish(); // Close the login activity
        }

        // Initialize UI elements
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);

        // Set click listener for the Login button
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        // Get the email and password from the input fields
        String email = Objects.requireNonNull(emailInputLayout.getEditText()).getText().toString();
        String password = Objects.requireNonNull(passwordInputLayout.getEditText()).getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showSnackbar("Please fill in all the entries");
            return;
        }

        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                // Add a listener to handle the result of the login attempt
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        showSnackbar("Welcome, " + user.getDisplayName());

                        // Redirect to the main activity
                        Intent intent = new Intent(LoginActivity.this, SkeletonActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthException e) {
                            String errorCode = e.getErrorCode();
                            if ("ERROR_WRONG_PASSWORD".equals(errorCode)) {
                                showSnackbar("Invalid password. Please try again.");
                            } else if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                                showSnackbar("Email not registered. Please sign up.");
                            } else {
                                showSnackbar("Login failed. Please try again.");
                            }
                        } catch (Exception e) {
                            showSnackbar("Login failed. Please try again.");
                        }
                    }
                });
    }

    private void showSnackbar(String message) {
        // Show a snackbar with the given message
        View view = findViewById(android.R.id.content); // Use the root view of your activity
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
