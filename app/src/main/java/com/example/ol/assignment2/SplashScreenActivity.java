package com.example.ol.assignment2;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends AppCompatActivity {
    private TextView txtNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timer timer;
        // Write a message to the database
        txtNames = findViewById(R.id.txtNames);
        Typeface myFont = Typeface.createFromAsset(this.getAssets(), "fonts/Champagne & Limousines Bold.ttf");
        txtNames.setTypeface(myFont);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
