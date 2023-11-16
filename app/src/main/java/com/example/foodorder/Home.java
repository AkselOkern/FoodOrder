package com.example.foodorder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Home extends Fragment {

    private static final String TAG = "HomeFragment";

    private List<Pizza> pizzaList;
    private PizzaAdapter pizzaAdapter;
    private SearchView searchView;

    public Home() {
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the SearchView
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search logic here if needed
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

        return view;
    }

    // ViewHolder for each pizza card
    public class PizzaAdapter extends RecyclerView.Adapter<PizzaViewHolder> implements Filterable {

        private List<Pizza> pizzaList;
        private List<Pizza> pizzaListFull;

        public PizzaAdapter(List<Pizza> pizzaList) {
            this.pizzaList = pizzaList;
            this.pizzaListFull = new ArrayList<>(pizzaList);
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

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<Pizza> filteredList = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(pizzaListFull);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        for (Pizza pizza : pizzaListFull) {
                            if (pizza.getItemName().toLowerCase().contains(filterPattern)) {
                                filteredList.add(pizza);
                            }
                        }
                    }

                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    pizzaList.clear();
                    pizzaList.addAll((List<Pizza>) results.values);
                    notifyDataSetChanged();
                    Log.d(TAG, "Filtered Pizza List Size: " + pizzaList.size());
                }
            };
        }

        public void setFullPizzaList(List<Pizza> pizzaList) {
            this.pizzaListFull = new ArrayList<>(pizzaList);
            notifyDataSetChanged();
        }
    }

    public class PizzaViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewPizza;
        private final TextView textViewItemName;
        private final TextView textViewPrice;
        private final TextView textViewQuantity;
        private final Button btnDecrease;
        private final Button btnIncrease;
        private final Button btnAddToCart;
        private final androidx.appcompat.widget.AppCompatSpinner spinnerPizzaSize;

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
            Glide.with(itemView.getContext())
                    .load(pizza.getImagePath())
                    .placeholder(R.drawable.placeholder_image_loading)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPizza);

            textViewItemName.setText(pizza.getItemName());
            textViewPrice.setText(String.format("Price: NOK%.2f", pizza.getPrice()));

            int sizePosition = Arrays.asList(getResources().getStringArray(R.array.pizza_sizes)).indexOf(pizza.getSize());
            spinnerPizzaSize.setSelection(sizePosition);

            final int[] quantity = {1};
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

            btnAddToCart.setOnClickListener(v -> addToCart(pizza, quantity[0], spinnerPizzaSize.getSelectedItem().toString()));
        }

        private void addToCart(Pizza pizza, int quantity, String size) {
            String message = quantity + " " + size + " " + pizza.getItemName() + "(s) added to cart";
            View view = getView();
            if (view != null) {
                Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
