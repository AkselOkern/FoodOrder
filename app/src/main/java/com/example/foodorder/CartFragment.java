package com.example.foodorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CartFragment extends Fragment {

    private ArrayList<CartItem> cartItemsList;
    private CartAdapter cartAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnPlaceOrder;
    private String orderType = "Pickup"; // Default order type
    private SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private TextView textViewTotalPrice;
    private static final int LOCATION_REQUEST_CODE = 100; // Define the location request code

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
        textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice);

        Button btnPickup = view.findViewById(R.id.btnPickup);
        Button btnDelivery = view.findViewById(R.id.btnDelivery);
        FrameLayout mapContainer = view.findViewById(R.id.mapContainer);

        btnPickup.setSelected(true); // Set Pickup as default option
        btnPickup.setTextColor(Color.WHITE); // Set text color darker for selected button

        btnPickup.setOnClickListener(v -> {
            // Set order type to Pickup
            orderType = "Pickup";
            btnPickup.setSelected(true);
            btnDelivery.setSelected(false);
            btnPickup.setTextColor(Color.WHITE);
            btnDelivery.setTextColor(Color.parseColor("#ffadad"));
            updateMap();
        });

        btnDelivery.setOnClickListener(v -> {
            // Set order type to Delivery
            orderType = "Delivery";
            btnDelivery.setSelected(true);
            btnPickup.setSelected(false);
            btnDelivery.setTextColor(Color.WHITE);
            btnPickup.setTextColor(Color.parseColor("#ffadad"));
            updateMap();
        });

        // Initialize Map
        supportMapFragment = SupportMapFragment.newInstance(); // Create a new instance of SupportMapFragment
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction(); // Get FragmentManager and start a transaction
        fragmentTransaction.add(mapContainer.getId(), supportMapFragment); // Add the fragment to the 'mapContainer' FrameLayout
        fragmentTransaction.commit(); // Commit the transaction

        supportMapFragment.getMapAsync(map -> {
            // Get the GoogleMap object from the fragment
            googleMap = map;
            updateMap();
        });

        TextView textViewCartTitle = view.findViewById(R.id.textViewCartTitle);
        ListView listViewCartItems = view.findViewById(R.id.listViewCartItems);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        cartItemsList = getCartItemsFromSharedPreferences();
        if (cartItemsList == null) {
            // Initialize cartItemsList if it is null
            cartItemsList = new ArrayList<>();
        }
        cartAdapter = new CartAdapter(getContext(), cartItemsList, this);

        listViewCartItems.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> {
            // Check if cart is empty
            if (!cartItemsList.isEmpty()) {
                // Place order
                placeOrder();
            } else {
                // Handle empty cart
                Snackbar.make(view, "Your cart is empty!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Calculate total price after initializing cartItemsList
        double totalPrice = calculateTotalPrice(cartItemsList);
        textViewTotalPrice.setText(String.format("Sum: %.2fkr", totalPrice));

        return view;
    }


    @SuppressLint("DefaultLocale")
    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice(cartItemsList);
        // Update total price
        if (textViewTotalPrice != null) {
            textViewTotalPrice.setText(String.format("Sum: %.2fkr", totalPrice));
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Update place order button state
        super.onViewCreated(view, savedInstanceState);
        updatePlaceOrderButtonState();
    }

    private void updatePlaceOrderButtonState() {
        // Enable place order button if cart is not empty
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setEnabled(cartItemsList != null && !cartItemsList.isEmpty());
        }
    }

    private void updateMap() {
        // Update map based on order type
        if (googleMap != null) {
            googleMap.clear(); // Clear previous markers

            if (orderType.equals("Delivery")) {
                // Logic to display user's location for delivery
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

                // Check if location permission is granted
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                        }
                    });
                } else {
                    // Request location permission
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
            } else if (orderType.equals("Pickup")) {
                // Logic to set a specific coordinate for pickup
                LatLng pickupCoordinate = new LatLng(60.1649342, 10.2548346);
                googleMap.addMarker(new MarkerOptions().position(pickupCoordinate).title("Luigi's pizzeria"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupCoordinate, 17));
            }
        }
    }

    // Method to place an order
    @SuppressLint("DefaultLocale")
    private void placeOrder() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Check if user is logged in
        if (currentUser != null) {
            // Retrieve user details from Firebase users collection
            String userEmail = currentUser.getEmail();
            CollectionReference usersCollection = db.collection("users");
            Query query = usersCollection.whereEqualTo("email", userEmail);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String zipcode = documentSnapshot.getString("zipCode");
                        String city = documentSnapshot.getString("city");

                        // Construct order object or map with required details
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("firstName", firstName);
                        orderData.put("lastName", lastName);
                        orderData.put("phone", phone);
                        orderData.put("address", address);
                        orderData.put("zipcode", zipcode);
                        orderData.put("city", city);

                        // Add orderType to order data
                        orderData.put("orderType", orderType);

                        // Add cart items to order
                        ArrayList<Map<String, Object>> cartItemsData = new ArrayList<>();
                        double totalPrice = 0.0; // Initialize total price
                        for (CartItem cartItem : cartItemsList) {
                            // Construct cart item object or map with required details
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("itemName", cartItem.getItemName());
                            itemData.put("size", cartItem.getSize());
                            itemData.put("quantity", cartItem.getQuantity());
                            itemData.put("price", cartItem.getItemPrice());
                            cartItemsData.add(itemData);

                            totalPrice += (cartItem.getItemPrice());
                        }
                        orderData.put("cartItems", cartItemsData);
                        orderData.put("totalPrice", totalPrice);

                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        orderData.put("date", timestamp);

                        // Save order details to the "orders" collection
                        CollectionReference ordersCollection = db.collection("orders");
                        ordersCollection.add(orderData)
                                .addOnSuccessListener(documentReference -> {
                                    // Handle successful order placement
                                    clearCartItems();
                                    cartAdapter.notifyDataSetChanged();
                                    //Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(requireView(), "Order Placed Successfully!", Snackbar.LENGTH_SHORT).show();
                                    textViewTotalPrice.setText(String.format("Sum: %.2fkr", 0.0));
                                })
                                .addOnFailureListener(e -> {
                                    // Handle order placement failure
                                    Snackbar.make(requireView(), "Failed to place order. Please try again.", Snackbar.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Handle errors while fetching user data
                    Snackbar.make(requireView(), "Failed to retrieve user data. Please try again.", Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }


    // Method to retrieve cart items from SharedPreferences
    private ArrayList<CartItem> getCartItemsFromSharedPreferences() {
        // Retrieve cart items from SharedPreferences
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cartItems", ""); // Retrieve cart items from SharedPreferences
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void clearCartItems() {
        // Clear cart items from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cartItems");
        editor.apply();
        if (cartItemsList != null) {
            cartItemsList.clear();
        }
    }

    private double calculateTotalPrice(ArrayList<CartItem> cartItemsList) {
        // Calculate total price of cart items
        double total = 0.0;
        for (CartItem cartItem : cartItemsList) {
            total += cartItem.getItemPrice();
        }
        return total;
    }

    private static class CartAdapter extends ArrayAdapter<CartItem> {
        private final ArrayList<CartItem> cartItemsList;
        private final Context context;
        private final CartFragment parentCartFragment; // Reference to the parent Cart class

        public CartAdapter(Context context, ArrayList<CartItem> cartItemsList, CartFragment parentCartFragment) {
            super(context, 0, cartItemsList);
            this.context = context;
            this.cartItemsList = cartItemsList;
            this.parentCartFragment = parentCartFragment; // Assign the reference to the parent Cart
        }


        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.cart_list_item, parent, false);
            }

            if (cartItemsList != null && !cartItemsList.isEmpty() && position < cartItemsList.size()) {
                // Get the CartItem object for this position
                CartItem currentItem = cartItemsList.get(position);

                // Set the data to your list item layout elements (e.g., TextViews)
                TextView itemNameTextView = listItem.findViewById(R.id.textViewCartItemName);
                TextView itemSizeTextView = listItem.findViewById(R.id.textViewCartItemSize);
                TextView itemQuantityTextView = listItem.findViewById(R.id.textViewCartItemQuantity);
                TextView itemPriceTextView = listItem.findViewById(R.id.textViewCartItemPrice);

                itemNameTextView.setText(currentItem.getItemName());
                itemSizeTextView.setText("Size: " + currentItem.getSize());
                itemQuantityTextView.setText("Quantity: " + currentItem.getQuantity());
                itemPriceTextView.setText("Price: " + currentItem.getItemPrice());

                Button deleteButton = listItem.findViewById(R.id.btnDeleteItem);
                deleteButton.setOnClickListener(v -> {
                    cartItemsList.remove(position);
                    notifyDataSetChanged();
                    saveCartItemsToSharedPreferences();
                    // Now, you can access updateTotalPrice() method using the parentCart reference
                    if (parentCartFragment != null) {
                        parentCartFragment.updateTotalPrice();
                    }
                });
            }

            return listItem;
        }

        private void saveCartItemsToSharedPreferences() {
            // Save cart items to SharedPreferences
            SharedPreferences.Editor editor = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE).edit(); // Get SharedPreferences editor
            Gson gson = new Gson(); // Create Gson object
            String json = gson.toJson(cartItemsList); // Convert cart items to JSON
            editor.putString("cartItems", json); // Save cart items to SharedPreferences
            editor.apply(); // Apply changes
        }
    }
}

