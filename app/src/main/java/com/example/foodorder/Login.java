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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

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

        // Initialize UI elements
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);

        // Set click listener for the Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailInputLayout.getEditText().getText().toString();
        String password = passwordInputLayout.getEditText().getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showSnackbar("Please fill all the entries");
            return;
        }

        // Sign in with Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = auth.getCurrentUser();
                        showSnackbar("Welcome, " + user.getDisplayName());

                        // Proceed to your desired activity
                        Intent intent = new Intent(Login.this, Hovedside.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed, show appropriate message
                        try {
                            throw task.getException();
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
        View view = findViewById(android.R.id.content); // Use the root view of your activity
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
//commit