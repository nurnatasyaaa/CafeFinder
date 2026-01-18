package com.example.cafefinder;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> favNames = new ArrayList<>();
    ArrayList<String> favPlaceIds = new ArrayList<>();

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        listView = findViewById(R.id.listFavourite);

        loadFavourites();
        checkPromotion();
        checkClosingSoon();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                favNames
        );

        listView.setAdapter(adapter);

        // 👉 CLICK → open details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, FavouriteDetailActivity.class);

            intent.putExtra("place_id", favPlaceIds.get(position));
            intent.putExtra("name", favNames.get(position));

            // OPTIONAL: if you saved these before
            intent.putExtra("address", "");
            intent.putExtra("rating", "");

            startActivity(intent);
        });


        // 👉 LONG PRESS → delete
        listView.setOnItemLongClickListener((parent, view, position, id) -> {

            new AlertDialog.Builder(this)
                    .setTitle("Remove Favourite")
                    .setMessage("Remove this café from favourites?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        deleteFavourite(position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true; // IMPORTANT
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavourites();
        adapter.notifyDataSetChanged();
    }

    // =========================
    // LOAD
    // =========================
    private void loadFavourites() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            SharedPreferences prefs =
                    getSharedPreferences("favourites_" + userId, MODE_PRIVATE);

            String json = prefs.getString("fav_list", "[]");

            JSONArray arr = new JSONArray(json);

            favNames.clear();
            favPlaceIds.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                favNames.add(obj.getString("name"));
                favPlaceIds.add(obj.getString("placeId"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load favourites", Toast.LENGTH_SHORT).show();
        }
    }


    // =========================
    // DELETE
    // =========================
    private void deleteFavourite(int position) {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            SharedPreferences prefs =
                    getSharedPreferences("favourites_" + userId, MODE_PRIVATE);

            String json = prefs.getString("fav_list", "[]");
            JSONArray arr = new JSONArray(json);
            JSONArray newArr = new JSONArray();

            for (int i = 0; i < arr.length(); i++) {
                if (i != position) {
                    newArr.put(arr.getJSONObject(i));
                }
            }

            // 🔥 THIS IS THE IMPORTANT LINE
            prefs.edit().putString("fav_list", newArr.toString()).apply();

            favNames.remove(position);
            favPlaceIds.remove(position);
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Favourite removed ⭐", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPromotion() {
        if (favNames.size() > 0) {
            NotificationHelper.send(
                    this,
                    "🔥 Promotion Alert",
                    "Special promo available for your favourite café!",
                    "PROMO"
            );
        }
    }
    private void checkClosingSoon() {
        int hour = java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 20 && favNames.size() > 0) {
            NotificationHelper.send(
                    this,
                    "⏰ Closing Soon",
                    "Your favourite café is closing soon. Hurry up!",
                    "CLOSING"
            );
        }
    }
}
