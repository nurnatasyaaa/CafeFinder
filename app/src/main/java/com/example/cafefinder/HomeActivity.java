package com.example.cafefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void openMap(View v) {
        startActivity(new Intent(this, MapActivity.class));
    }

    public void openFavourite(View v) {
        startActivity(new Intent(this, FavouriteActivity.class));
    }

    public void openQr(View v) {
        startActivity(new Intent(this, QrActivity.class));
    }

    public void openNotification(View v) {
        startActivity(new Intent(this, NotificationActivity.class));
    }

    public void openProfile(View v) {
        startActivity(new Intent(this, ProfileActivity.class));
    }
}
