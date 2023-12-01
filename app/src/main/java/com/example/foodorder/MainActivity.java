package com.example.foodorder;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private VideoView videoView;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        videoView = findViewById(R.id.backgroundVideo);

        String videoUrl = "https://cdn.lystad.io/foodorder/pizza_home.mp4";
        videoUri = Uri.parse(videoUrl);

        // Set click listener for the Log In button
        loginButton.setOnClickListener(v -> {
            // Navigate to the Login Activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Set click listener for the Register button
        registerButton.setOnClickListener(v -> {
            // Navigate to the Registration Activity
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start or restart the video when MainActivity is resumed
        startVideo();
    }

    private void startVideo() {
        if (videoUri != null) {
            videoView.setVideoURI(videoUri);
            videoView.setZOrderOnTop(false);
            videoView.getHolder().setFormat(PixelFormat.OPAQUE);

            videoView.start();

            videoView.setOnCompletionListener(mediaPlayer -> {
                // Restart the video
                videoView.start();
            });
        }
    }
}
