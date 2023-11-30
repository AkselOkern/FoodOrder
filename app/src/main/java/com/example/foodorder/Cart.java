package com.example.foodorder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class Cart extends Fragment {

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
            orderType = "Pickup";
            btnPickup.setSelected(true);
            btnDelivery.setSelected(false);
            btnPickup.setTextColor(Color.WHITE);
            btnDelivery.setTextColor(Color.parseColor("#ffadad"));
            updateMap();
        });

        btnDelivery.setOnClickListener(v -> {
            orderType = "Delivery";
            btnDelivery.setSelected(true);
            btnPickup.setSelected(false);
            btnDelivery.setTextColor(Color.WHITE);
            btnPickup.setTextColor(Color.parseColor("#ffadad"));
            updateMap();
        });

        // Initialize Map
        supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(mapContainer.getId(), supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {
                googleMap = map;
                updateMap();
            }
        });

        TextView textViewCartTitle = view.findViewById(R.id.textViewCartTitle);
        ListView listViewCartItems = view.findViewById(R.id.listViewCartItems);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        cartItemsList = getCartItemsFromSharedPreferences();
        if (cartItemsList == null) {
            cartItemsList = new ArrayList<>();
        }
        cartAdapter = new CartAdapter(getContext(), cartItemsList, this);

        listViewCartItems.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> {
            if (!cartItemsList.isEmpty()) {
                placeOrder();
            } else {
                Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // Calculate total price after initializing cartItemsList
        double totalPrice = calculateTotalPrice(cartItemsList);
        textViewTotalPrice.setText(String.format("Sum: %.2fkr", totalPrice));

        return view;
    }


    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice(cartItemsList);
        if (textViewTotalPrice != null) {
            textViewTotalPrice.setText(String.format("Sum: %.2fkr", totalPrice));
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePlaceOrderButtonState();
    }

    private void updatePlaceOrderButtonState() {
        if (btnPlaceOrder != null) {
            if (cartItemsList != null && !cartItemsList.isEmpty()) {
                btnPlaceOrder.setEnabled(true);
            } else {
                btnPlaceOrder.setEnabled(false);
            }
        }
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear(); // Clear previous markers

            if (orderType.equals("Delivery")) {
                // Logic to display user's location for delivery
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            }
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
            } else if (orderType.equals("Pickup")) {
                // Logic to set a specific coordinate for pickup
                LatLng pickupCoordinate = new LatLng(60.1649342, 10.2548346);
                googleMap.addMarker(new MarkerOptions().position(pickupCoordinate).title("Luigi's pizzeria"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupCoordinate, 15));
            }
        }
    }

    // Method to place an order
    private void placeOrder() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Retrieve user details from Firebase users collection
            String userEmail = currentUser.getEmail();
            CollectionReference usersCollection = db.collection("users");
            Query query = usersCollection.whereEqualTo("email", userEmail);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String zipcode = documentSnapshot.getString("zipCode");
                        String city = documentSnapshot.getString("city");

                        // Construct order object or map with required details
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("firstName", firstName);
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
                                    Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                                    textViewTotalPrice.setText(String.format("Sum: %.2fkr", 0.0));
                                })
                                .addOnFailureListener(e -> {
                                    // Handle order placement failure
                                    Toast.makeText(getContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Handle errors while fetching user data
                    Toast.makeText(getContext(), "Failed to retrieve user data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // Method to retrieve cart items from SharedPreferences
    private ArrayList<CartItem> getCartItemsFromSharedPreferences() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cartItems", "");
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void clearCartItems() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cartItems");
        editor.apply();
        if (cartItemsList != null) {
            cartItemsList.clear();
        }
    }

    private double calculateTotalPrice(ArrayList<CartItem> cartItemsList) {
        double total = 0.0;
        for (CartItem cartItem : cartItemsList) {
            total += cartItem.getItemPrice();
        }
        return total;
    }

    private static class CartAdapter extends ArrayAdapter<CartItem> {
        private final ArrayList<CartItem> cartItemsList;
        private final Context context;
        private final Cart parentCart; // Reference to the parent Cart class

        public CartAdapter(Context context, ArrayList<CartItem> cartItemsList, Cart parentCart) {
            super(context, 0, cartItemsList);
            this.context = context;
            this.cartItemsList = cartItemsList;
            this.parentCart = parentCart; // Assign the reference to the parent Cart
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.cart_list_item, parent, false);
            }

            if (cartItemsList != null && !cartItemsList.isEmpty() && position < cartItemsList.size()) {
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
                    if (parentCart != null) {
                        parentCart.updateTotalPrice();
                    }
                });
            }

            return listItem;
        }

        private void saveCartItemsToSharedPreferences() {
            SharedPreferences.Editor editor = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(cartItemsList);
            editor.putString("cartItems", json);
            editor.apply();
        }
    }
}

