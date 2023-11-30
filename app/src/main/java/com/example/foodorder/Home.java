package com.example.foodorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Home extends Fragment {

    private static final String TAG = "HomeFragment";

    private List<Pizza> pizzaList;
    private PizzaAdapter pizzaAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        TextInputLayout textInputLayout = view.findViewById(R.id.menu);
        MaterialAutoCompleteTextView spinnerFilterOptions = textInputLayout.findViewById(R.id.spinnerFilterOptions);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.filter_options,
                android.R.layout.simple_dropdown_item_1line
        );
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerFilterOptions.setAdapter(adapter);

        spinnerFilterOptions.setOnItemClickListener((parent, view1, position, id) -> filterPizzaList(position));
        SearchView searchView = view.findViewById(R.id.searchView);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        pizzaList = new ArrayList<>();
        pizzaAdapter = new PizzaAdapter(pizzaList);

        // Set up the RecyclerView with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(pizzaAdapter);

        // Query Firebase to get all pizza items
        CollectionReference pizzaCollection = firebaseFirestore.collection("pizza");
        pizzaCollection.orderBy("itemName").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pizzaList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Pizza pizza = document.toObject(Pizza.class);
                    pizzaList.add(pizza);
                }
                pizzaAdapter.setFullPizzaList(pizzaList); // Update the full pizza list
                pizzaAdapter.notifyDataSetChanged();
                Log.d(TAG, "Pizza List Size: " + pizzaList.size());
            } else {
                Toast.makeText(getActivity(), "Error fetching pizza items", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching pizza items", task.getException());
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query Text Changed: " + newText);
                // Filter the pizzaList based on the search query
                pizzaAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return view;
    }

    private void filterPizzaList(int filterOption) {
        switch (filterOption) {
            case 0:
                pizzaAdapter.sortByName();
                break;
            case 1:
                pizzaAdapter.sortByPriceAscending();
                break;
            case 2:
                pizzaAdapter.sortByPriceDescending();
                break;
        }
    }



    // ViewHolder for each pizza card
    public class PizzaAdapter extends RecyclerView.Adapter<PizzaViewHolder> implements Filterable {

        private final List<Pizza> pizzaList;
        private List<Pizza> pizzaListFull;

        public PizzaAdapter(List<Pizza> pizzaList) {
            this.pizzaList = pizzaList;
            this.pizzaListFull = new ArrayList<>(pizzaList);
        }

        @NonNull
        @Override
        public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout for each pizza card
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pizza, parent, false);
            return new PizzaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
            // Set the data for each pizza card
            Pizza pizza = pizzaList.get(position);
            holder.setPizzaData(pizza);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void sortByName() {
            // Sort the pizzaList by name
            pizzaList.sort((p1, p2) -> p1.getItemName().compareToIgnoreCase(p2.getItemName()));
            notifyDataSetChanged();
        }

        @SuppressLint("NotifyDataSetChanged")
        public void sortByPriceAscending() {
            // Sort the pizzaList by price
            pizzaList.sort(Comparator.comparingDouble(Pizza::getPrice));
            notifyDataSetChanged();
        }

        @SuppressLint("NotifyDataSetChanged")
        public void sortByPriceDescending() {
            // Sort the pizzaList by price
            pizzaList.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return pizzaList.size();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            // Filter the pizzaList based on the search query
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<Pizza> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        // If the search query is empty, show all items
                        filteredList.addAll(pizzaListFull);
                    } else {
                        // Filter the pizzaList based on the search query
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        for (Pizza pizza : pizzaListFull) {
                            // Filter by name
                            if (pizza.getItemName().toLowerCase().contains(filterPattern)) {
                                filteredList.add(pizza);
                            }
                        }
                    }

                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    // Update the pizzaList with the filtered results
                    pizzaList.clear();
                    pizzaList.addAll((List<Pizza>) results.values);
                    notifyDataSetChanged();
                    Log.d(TAG, "Filtered Pizza List Size: " + pizzaList.size());
                }
            };
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setFullPizzaList(List<Pizza> pizzaList) {
            // Update the full pizza list
            this.pizzaListFull = new ArrayList<>(pizzaList);
            notifyDataSetChanged();
        }
    }

    public class PizzaViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder for each pizza card

        private final ImageView imageViewPizza;
        private final TextView textViewItemName;
        private final TextView textViewPrice;
        private final TextView textViewQuantity;
        private final Button btnDecrease;
        private final Button btnIncrease;
        private final Button btnAddToCart;
        private final MaterialAutoCompleteTextView spinnerPizzaSize;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPizza = itemView.findViewById(R.id.imageViewPizza);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            spinnerPizzaSize = itemView.findViewById(R.id.spinnerPizzaSize);
        }

        @SuppressLint("DefaultLocale")
        public void setPizzaData(Pizza pizza) {
            // Set the data for each pizza card
            Glide.with(itemView.getContext()) // Load the pizza image
                    .load(pizza.getImagePath()) // Load the pizza image
                    .placeholder(R.drawable.placeholder_image_loading) // Set a placeholder while loading
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                    .into(imageViewPizza); // Set the image to the ImageView

            textViewItemName.setText(pizza.getItemName()); // Set the pizza name
            textViewPrice.setText(String.format("Price: NOK%.2f", pizza.getPrice())); // Format the price to 2 decimal places

            ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                    // Set the pizza size dropdown
                    itemView.getContext(),
                    R.array.pizza_sizes,
                    android.R.layout.simple_dropdown_item_1line
            );
            spinnerPizzaSize.setAdapter(sizeAdapter);

            int sizePosition = Arrays.asList(itemView.getResources().getStringArray(R.array.pizza_sizes)).indexOf(pizza.getSize());

            // Set the default selection to the first item if not found
            if (sizePosition == -1 && sizeAdapter.getCount() > 0) {
                // Set the default selection to the first item if not found
                spinnerPizzaSize.setText(Objects.requireNonNull(sizeAdapter.getItem(0)).toString(), false);
            } else if (sizePosition != -1) {
                // Set the default selection to the pizza size if found
                spinnerPizzaSize.setText(Objects.requireNonNull(sizeAdapter.getItem(sizePosition)).toString(), false); // Set initial selection without triggering listener
            }

            final int[] quantity = {1};
            textViewQuantity.setText(String.valueOf(quantity[0]));

            btnDecrease.setOnClickListener(v -> {
                // Decrease the quantity
                if (quantity[0] > 1) {
                    quantity[0]--;
                    textViewQuantity.setText(String.valueOf(quantity[0]));
                }
            });

            btnIncrease.setOnClickListener(v -> {
                // Increase the quantity
                quantity[0]++;
                textViewQuantity.setText(String.valueOf(quantity[0]));
            });

            btnAddToCart.setOnClickListener(v -> addToCart(pizza, quantity[0], spinnerPizzaSize.getText().toString(), pizza.getPrice())); // Add the pizza to cart
        }
        private void addToCart(Pizza pizza, int quantity, String size, double price) {
            // Create or access SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);

            // Retrieve existing cart items
            Gson gson = new Gson();
            String json = sharedPreferences.getString("cartItems", "");
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            ArrayList<CartItem> cartItems = gson.fromJson(json, type);

            // If cartItems is null, create a new ArrayList
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }

            // Calculate the total price based on the pizza size
            // We did not have time to implement the logic for calculating the price based on the pizza size
            double totalPrice;
            if (size.equals("Medium")) {
                totalPrice = price * 1.5 * quantity;
            } else if (size.equals("Large")) {
                totalPrice = price * 2.5 * quantity;
            } else {
                totalPrice = price * quantity;
            }

            // Add the new item to the cart
            CartItem cartItem = new CartItem(pizza.getItemName(), size, quantity, totalPrice);
            cartItems.add(cartItem);

            // Convert cartItems to JSON and save to SharedPreferences
            String updatedCart = gson.toJson(cartItems);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("cartItems", updatedCart);
            editor.apply();

            String message = quantity + " " + size + " " + pizza.getItemName() + "(s) added to cart";
            View view = getView();
            if (view != null) {
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
            }
        }

    }
}
