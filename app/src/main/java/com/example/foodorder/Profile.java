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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class Profile extends Fragment {
    private TextView firstNameTextView, lastNameTextView, emailTextView, phoneTextView, addressTextView, zipcodeTextView, cityTextView;
    private Button editAddressButton, deleteProfileButton, logoutButton;

    private FirebaseAuth firebaseAuth;
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userDataListener;
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

            DocumentReference userRef = db.collection("users").document(userEmail);
            userDataListener = userRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    // Handle errors
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Extract user data from the document
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String address = documentSnapshot.getString("address");
                    String zipcode = documentSnapshot.getString("zipcode");
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
            });
        }
    }


    private void editAddress() {

    }


    private void deleteProfile() {

    }

        private void logout () {
            firebaseAuth.signOut();
            // Navigate back to MainActivity
            requireActivity().finish();
        }
}

