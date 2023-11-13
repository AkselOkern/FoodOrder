package com.example.foodorder;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Hovedside extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    //private static final String TAG = "Hovedside";


    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hovedside);
        Log.d("Hovedside", "onCreate");
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

    }

    Home homeFragment = new Home();
    Map mapFragment = new Map();
    Cart cartFragment = new Cart();
    Profile profileFragment = new Profile();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("Hovedside", "onNavigationItemSelected: " + item.getItemId());
        if (item.getItemId() == R.id.navigation_home) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, homeFragment).commit();
            return true;
        } else if (item.getItemId() == R.id.navigation_map) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, mapFragment).commit();
            return true;
        } else if (item.getItemId() == R.id.navigation_cart) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, cartFragment).commit();
            return true;
        } else if (item.getItemId() == R.id.navigation_profile) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, profileFragment).commit();
            return true;
        }
        return false;
    }

}
//commit
