package com.example.foodorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Initialize FirebaseAuth and FirebaseFirestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);

        TextView textViewCartTitle = view.findViewById(R.id.textViewCartTitle);
        ListView listViewCartItems = view.findViewById(R.id.listViewCartItems);
        Button btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        cartItemsList = getCartItemsFromSharedPreferences(); // Retrieve cart items from SharedPreferences
        if (cartItemsList == null) {
            cartItemsList = new ArrayList<>(); // Initialize cartItemsList if null
        }

        cartAdapter = new CartAdapter(getContext(), cartItemsList);
        listViewCartItems.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> {
            // Retrieve user details from Firebase users collection
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
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
        });

        return view;
    }

    // Method to retrieve cart items from SharedPreferences
    private ArrayList<CartItem> getCartItemsFromSharedPreferences() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("cartItems", "");
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        ArrayList<CartItem> cartItems = gson.fromJson(json, type);

        // If cartItems is null, initialize it as an empty list
        return cartItems != null ? cartItems : new ArrayList<>();
    }

    // Method to clear cart items
    private void clearCartItems() {
        // Clear cart items from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cartItems");
        editor.apply();

        if (cartItemsList != null) {
            cartItemsList.clear(); // Clear the cart items list if not null
        }
    }

    // Adapter for displaying cart items in the ListView
    private static class CartAdapter extends ArrayAdapter<CartItem> {

        private final ArrayList<CartItem> cartItemsList;
        private final Context context;

        public CartAdapter(Context context, ArrayList<CartItem> cartItemsList) {
            super(context, 0, cartItemsList);
            this.context = context;
            this.cartItemsList = cartItemsList;
        }

        @SuppressLint("SetTextI18n")
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
                itemPriceTextView.setText("Price: "+ currentItem.getItemPrice());

                // Handle delete button click
                Button deleteButton = listItem.findViewById(R.id.btnDeleteItem);
                deleteButton.setOnClickListener(v -> {
                    // Remove item from the list and update the adapter
                    cartItemsList.remove(position);
                    notifyDataSetChanged();
                    saveCartItemsToSharedPreferences(); // Save updated cart items to SharedPreferences
                });
            }

            return listItem;
        }

        // Add a method to save updated cart items to SharedPreferences
        private void saveCartItemsToSharedPreferences() {
            SharedPreferences.Editor editor = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE).edit();
            Gson gson = new Gson();
            String json = gson.toJson(cartItemsList);
            editor.putString("cartItems", json);
            editor.apply();
        }
    }
}
