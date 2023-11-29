package com.example.foodorder;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        VideoView videoView = findViewById(R.id.backgroundVideo);


        String videoUrl = "https://cdn.lystad.io/foodorder/pizza_home.mp4";
        Uri uri = Uri.parse(videoUrl);

        videoView.setVideoURI(uri);
        videoView.setZOrderOnTop(false);
        videoView.getHolder().setFormat(PixelFormat.OPAQUE);

        videoView.start();

        videoView.setOnCompletionListener(mediaPlayer -> {
            // Restart the video
            videoView.start();
        });



        // Set click listener for the Log In button
        loginButton.setOnClickListener(v -> {
            // Navigate to the Login Activity
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });

        // Set click listener for the Register button
        registerButton.setOnClickListener(v -> {
            // Navigate to the Registration Activity
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}


