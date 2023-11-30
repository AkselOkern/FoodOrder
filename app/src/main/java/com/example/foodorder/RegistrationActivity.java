package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText addressEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextInputEditText zipCodeEditText;
    private TextInputEditText cityEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI elements
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        zipCodeEditText = findViewById(R.id.zipCodeEditText);
        cityEditText = findViewById(R.id.cityEditText);
        registerButton = findViewById(R.id.registerButton);

        // Set click listener for the Register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        // Get user input
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String zipCode = zipCodeEditText.getText().toString();
        String city = cityEditText.getText().toString();

        // Check if any field is blank
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty() || zipCode.isEmpty() || city.isEmpty()) {
            showSnackbar("Please fill all the entries");
            return; // Stop further processing
        }

        // Check if passwords match
        if (!password.equals(confirmPasswordEditText.getText().toString())) {
            showSnackbar("Passwords do not match");
            return; // Stop further processing
        }

        // Check if the password is at least 6 characters long
        if (password.length() < 6) {
            showSnackbar("Password must be 6 or more characters");
            return; // Stop further processing
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Create a new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration and user creation are successful
                        FirebaseUser user = auth.getCurrentUser();
                        // Now you are logged in as the new user
                        showSnackbar("Registration Successful");

                        // Proceed to store user data in Firestore and navigate to the Hovedside activity
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference usersCollection = db.collection("users");

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("firstName", firstName);
                        userData.put("lastName", lastName);
                        userData.put("email", email);
                        userData.put("phone", phone);
                        userData.put("address", address);
                        userData.put("zipCode", zipCode);
                        userData.put("city", city);

                        // Add the user data to the "users" collection
                        usersCollection.add(userData)
                                .addOnSuccessListener(documentReference -> {
                                    // Data added successfully
                                    // Now navigate to the Hovedside activity
                                    Intent intent = new Intent(RegistrationActivity.this, SkeletonActivity.class);
                                    startActivity(intent);
                                    finish(); // Close the current activity
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the failure to add data
                                    showSnackbar("Data storage failed. Please try again.");
                                });
                    } else {
                        // Registration failed
                        showSnackbar("Registration failed. Please try again.");
                    }
                });
    }

    private void showSnackbar(String message) {
        View view = findViewById(android.R.id.content); // Use the root view of your activity
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


}
