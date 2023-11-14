package com.example.foodorder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private List<Pizza> pizzaList;
    private PizzaAdapter pizzaAdapter;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        firebaseFirestore = FirebaseFirestore.getInstance();
        pizzaList = new ArrayList<>();
        pizzaAdapter = new PizzaAdapter(pizzaList);

        // Set up the RecyclerView with the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(pizzaAdapter);

        // Query to get all pizza items
        CollectionReference pizzaCollection = firebaseFirestore.collection("pizza");
        pizzaCollection.orderBy("itemName").get().addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    pizzaList.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        Pizza pizza = document.toObject(Pizza.class);
                        pizzaList.add(pizza);
                    }
                    pizzaAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error fetching pizza items", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // ViewHolder for each pizza card
    private class PizzaAdapter extends RecyclerView.Adapter<PizzaViewHolder> {

        private List<Pizza> pizzaList;

        public PizzaAdapter(List<Pizza> pizzaList) {
            this.pizzaList = pizzaList;
        }

        @NonNull
        @Override
        public PizzaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_pizza, parent, false);
            return new PizzaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PizzaViewHolder holder, int position) {
            Pizza pizza = pizzaList.get(position);
            holder.setPizzaData(pizza);
        }

        @Override
        public int getItemCount() {
            return pizzaList.size();
        }
    }

    // ViewHolder for each pizza card
// Inside PizzaViewHolder class
    public class PizzaViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewPizza;
        private TextView textViewItemName;
        private TextView textViewPrice;
        private TextView textViewQuantity;
        private Button btnDecrease;
        private Button btnIncrease;
        private Button btnAddToCart;

        public PizzaViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            imageViewPizza = itemView.findViewById(R.id.imageViewPizza);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        @SuppressLint("DefaultLocale")
        public void setPizzaData(Pizza pizza) {
            // Set data to views in the card layout

            // Use Glide for image loading
            Glide.with(itemView.getContext())
                    .load(pizza.getImagePath())
                    .placeholder(R.drawable.placeholder_image_loading) // Replace with your placeholder
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Optional: Cache the image
                    .into(imageViewPizza);

            textViewItemName.setText(pizza.getItemName());
            textViewPrice.setText(String.format("Price: NOK%.2f", pizza.getPrice())); // Format price to display two decimal places

            // Quantity handling
            final int[] quantity = {1}; // Default quantity
            textViewQuantity.setText(String.valueOf(quantity[0]));

            btnDecrease.setOnClickListener(v -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    textViewQuantity.setText(String.valueOf(quantity[0]));
                }
            });

            btnIncrease.setOnClickListener(v -> {
                quantity[0]++;
                textViewQuantity.setText(String.valueOf(quantity[0]));
            });

            // Add to cart logic
            btnAddToCart.setOnClickListener(v -> addToCart(pizza, quantity[0]));
        }

        // Implement a method to handle adding to the cart
        private void addToCart(Pizza pizza, int quantity) {
            // Add logic to add the selected pizza with the specified quantity to the cart
            // You can use this method to trigger an action when the "Add to Cart" button is clicked
            // Example: cartManager.addToCart(pizza, quantity);
        }
    }

}
