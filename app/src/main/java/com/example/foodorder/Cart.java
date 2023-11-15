package com.example.foodorder;

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
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Cart extends Fragment {

    private ArrayList<CartItem> cartItemsList;
    private CartAdapter cartAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);

        TextView textViewCartTitle = view.findViewById(R.id.textViewCartTitle);
        ListView listViewCartItems = view.findViewById(R.id.listViewCartItems);
        Button btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);

        cartItemsList = getCartItemsFromSharedPreferences(); // Retrieve cart items from SharedPreferences

        cartAdapter = new CartAdapter(getContext(), cartItemsList);
        listViewCartItems.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> {
            // Place order logic here
            // Implement your logic to process the order

            // Clear the cart items list
            clearCartItems();
            cartAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
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

        // Clear the cart items list locally
        cartItemsList.clear();
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

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.cart_list_item, parent, false);
            }

            CartItem currentItem = cartItemsList.get(position);

            // Set the data to your list item layout elements (e.g., TextViews)
            TextView itemNameTextView = listItem.findViewById(R.id.textViewCartItemName);
            TextView itemSizeTextView = listItem.findViewById(R.id.textViewCartItemSize);
            TextView itemQuantityTextView = listItem.findViewById(R.id.textViewCartItemQuantity);

            itemNameTextView.setText(currentItem.getItemName());
            itemSizeTextView.setText("Size: " + currentItem.getSize());
            itemQuantityTextView.setText("Quantity: " + currentItem.getQuantity());

            return listItem;
        }
    }
}
