package com.example.foodorder;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ProfileFragment extends Fragment {
    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneTextView, addressTextView, zipcodeTextView, cityTextView;
    private Button editAddressButton, deleteProfileButton, logoutButton;

    private FirebaseAuth firebaseAuth;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userDataListener;
    private FirebaseFirestore firestore;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false); // Inflate the layout for this fragment
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
        editAddressButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_edit_address, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            TextInputEditText editAddress = dialogView.findViewById(R.id.addressEditText);
            TextInputEditText editZipcode = dialogView.findViewById(R.id.zipcodeEditText);
            TextInputEditText editCity = dialogView.findViewById(R.id.cityEditText);

            // Set initial values if needed
            editAddress.setText(addressTextView.getText().toString());
            editZipcode.setText(zipcodeTextView.getText().toString());
            editCity.setText(cityTextView.getText().toString());

            Button btnCancel = dialogView.findViewById(R.id.buttonCancel);
            Button btnSave = dialogView.findViewById(R.id.buttonSave);

            btnCancel.setOnClickListener(cancelView -> {
                // Dismiss the dialog when Cancel button is clicked
                dialog.dismiss();
            });

            btnSave.setOnClickListener(saveView -> {
                // Perform save action here, e.g., update user data
                editAddress(
                        Objects.requireNonNull(editAddress.getText()).toString(),
                        Objects.requireNonNull(editZipcode.getText()).toString(),
                        Objects.requireNonNull(editCity.getText()).toString()
                );

                // Dismiss the dialog after initiating save action
                dialog.dismiss();

                // Auto-refresh profile data after updating
                // Update the TextViews or UI elements displaying the profile data
                addressTextView.setText(editAddress.getText().toString());
                zipcodeTextView.setText(editZipcode.getText().toString());
                cityTextView.setText(editCity.getText().toString());
            });

            dialog.show();
        });
        deleteProfileButton.setOnClickListener(v -> {
            // Show confirmation dialog before deleting the profile
            // Inside a Fragment
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            View cancelDialogProfileButton = dialogView.findViewById(R.id.btnCancel);
            View deleteDialogProfileButton = dialogView.findViewById(R.id.btnDelete);

            cancelDialogProfileButton.setOnClickListener(cancelView -> {
                // Dismiss the dialog when Cancel button is clicked
                dialog.dismiss();
            });

            deleteDialogProfileButton.setOnClickListener(deleteView -> {
                // Call the deleteProfile() method when Delete button is clicked
                deleteProfile();
                // Dismiss the dialog after initiating profile deletion
                dialog.dismiss();
            });

            dialog.show(); // Show the dialog
        });

        logoutButton.setOnClickListener(v -> logout());
        

        // Initialize Firebase again (add these lines)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            // Retrieve user data from Firestore

            CollectionReference usersCollection = db.collection("users");
            Query query = usersCollection.whereEqualTo("email", userEmail);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        // Retrieve user data
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String zipcode = documentSnapshot.getString("zipCode");
                        String city = documentSnapshot.getString("city");

                        // Set TextViews with retrieved data
                        firstNameTextView.setText(firstName);
                        lastNameTextView.setText(lastName);
                        emailTextView.setText(email);
                        phoneTextView.setText(phone);
                        addressTextView.setText(address);
                        zipcodeTextView.setText(zipcode);
                        cityTextView.setText(city);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }



    private void editAddress(String address, String zipCode, String city) {
        // Update user data in Firestore
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is logged in, proceed with updating
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersCollection = db.collection("users");

            Query query = usersCollection.whereEqualTo("email", userEmail);
            // Query query = usersCollection.whereEqualTo("email", userEmail).limit(1);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Update user details
                        usersCollection.document(document.getId()).update(
                                "address", address,
                                "zipCode", zipCode,
                                "city", city
                        ).addOnSuccessListener(aVoid -> {
                            // Update successful
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating document", e);
                        });
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }


    private void deleteProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, proceed with deletion
            String userEmail = currentUser.getEmail();

            // Deleting user from Firestore
            CollectionReference usersCollection = db.collection("users");
            Query query = usersCollection.whereEqualTo("email", userEmail);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Delete documents with the matched email
                        document.getReference().delete();
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });

            // Deleting user from Firebase Authentication
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firebaseAuth.signOut();
                    // Navigate back to MainActivity
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
                    startActivity(intent);
                    requireActivity().finish();
                    // Account deleted successfully
                } else {
                    Log.e(TAG, "Error deleting account", task.getException());
                }
            });
        }
    }

    private void logout() {
        firebaseAuth.signOut();

        // Navigate back to MainActivity
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity
    }
}

