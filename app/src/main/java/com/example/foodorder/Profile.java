package com.example.foodorder;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends Fragment {
    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneTextView, addressTextView, zipcodeTextView, cityTextView;
    private Button editAddressButton, deleteProfileButton, logoutButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        firstNameTextView = view.findViewById(R.id.firstNameTextView);
        lastNameTextView = view.findViewById(R.id.lastNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        zipcodeTextView = view.findViewById(R.id.zipcodeTextView);
        cityTextView = view.findViewById(R.id.cityTextView);

        editAddressButton = view.findViewById(R.id.editAddressButton);
        deleteProfileButton = view.findViewById(R.id.deleteProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Set click listeners
        editAddressButton.setOnClickListener(v -> editAddress());
        deleteProfileButton.setOnClickListener(v -> deleteProfile());
        logoutButton.setOnClickListener(v -> logout());

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String email = document.getString("email");
                                String phone = document.getString("phone");
                                String address = document.getString("address");
                                String zipcode = document.getString("zipcode");
                                String city = document.getString("city");

                                // Verify the email before displaying data
                                if (currentUser.getEmail().equals(email)) {
                                    // Set user data to TextViews
                                    firstNameTextView.setText(firstName);
                                    lastNameTextView.setText(lastName);
                                    emailTextView.setText(email);
                                    phoneTextView.setText(phone);
                                    addressTextView.setText(address);
                                    zipcodeTextView.setText(zipcode);
                                    cityTextView.setText(city);
                                } else {
                                    Log.e(TAG, "fiks koden display", task.getException());
                                }
                            }
                        }
                    });
        }
    }

    private void editAddress() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String email = document.getString("email");

                                // Verify the email before editing the address
                                if (currentUser.getEmail().equals(email)) {
                                    // Implement logic for editing address
                                } else {
                                    Log.e(TAG, "fiks koden adresse", task.getException());
                                }
                            }
                        }
                    });
        }
    }

    private void deleteProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String email = document.getString("email");

                                // Verify the email before showing the confirmation dialog
                                if (currentUser.getEmail().equals(email)) {
                                    // Create a confirmation dialog
                                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                                    builder.setTitle("Delete Profile");
                                    builder.setMessage("Are you sure you want to delete your profile? This action cannot be undone.");

                                    builder.setPositiveButton("Delete", (dialog, which) -> deleteAccount());

                                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                                    // Show the confirmation dialog
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else {
                                    Log.e(TAG, "fiks koden delete", task.getException());
                                }
                            }
                        }
                    });
        }
    }

    private void deleteUserDataFromFirestore(String userId) {
        // Get the reference to the "users" collection
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Delete the user's document from the collection
        usersCollection.document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data deleted from Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting user data from Firestore", e));
    }
    private void deleteAccount() {
        // Get user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Delete the user from Firebase Authentication
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            deleteUserDataFromFirestore(user.getUid());

                            // Redirect user
                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                            startActivity(intent);
                            requireActivity().finish(); // Close the current activity
                        } else {
                            Log.e(TAG, "Error deleting user account", task.getException());

                        }
                    });
        }
    }

        private void logout () {
            firebaseAuth.signOut();
            // Navigate back to MainActivity
            requireActivity().finish();
        }
}

