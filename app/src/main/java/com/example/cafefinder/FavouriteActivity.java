package com.example.cafefinder;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    RecyclerView recycler;
    FavouriteAdapter favouriteAdapter;

    ArrayList<String> favNames = new ArrayList<>();
    ArrayList<String> favPlaceIds = new ArrayList<>();
    ArrayList<String> favPhotoRefs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        recycler = findViewById(R.id.recyclerFavourite);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadFavourites(); // must be BEFORE adapter
        checkPromotion();
        checkClosingSoon();

        favouriteAdapter = new FavouriteAdapter(
                this,
                favNames
        );

        recycler.setAdapter(favouriteAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavourites();
        favouriteAdapter.notifyDataSetChanged();
    }

    // =========================
    // LOAD FAVOURITES
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
            favPhotoRefs.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                favNames.add(obj.getString("name"));
                favPlaceIds.add(obj.getString("placeId"));
                favPhotoRefs.add(obj.optString("photoRef", ""));
            }


        } catch (Exception e) {
            Toast.makeText(this, "Failed to load favourites", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================
    // DELETE FAVOURITE
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

            prefs.edit().putString("fav_list", newArr.toString()).apply();

            favNames.remove(position);
            favPlaceIds.remove(position);
            favouriteAdapter.notifyItemRemoved(position);

            Toast.makeText(this, "Favourite removed ⭐", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================
    // CONFIRM DELETE
    // =========================
    public void confirmDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Favourite")
                .setMessage("Remove this café from favourites?")
                .setPositiveButton("Remove", (d, w) -> deleteFavourite(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =========================
    // OPTIONAL NOTIFICATIONS
    // =========================
    private void checkPromotion() {
        if (!favNames.isEmpty()) {
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

        if (hour >= 20 && !favNames.isEmpty()) {
            NotificationHelper.send(
                    this,
                    "⏰ Closing Soon",
                    "Your favourite café is closing soon. Hurry up!",
                    "CLOSING"
            );
        }
    }
}
