package com.example.cafefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class PlaceDetailsActivity extends AppCompatActivity {

    ImageView imagePlace;
    TextView txtName, txtAddress, txtPhone, txtRating, txtWebsite, txtOpenNow;
    Button btnFavourite;
    String photoRef = "";

    String placeId;
    String API_KEY = "YOUR_API_KEY_HERE";

    double lat = 0, lng = 0; // for navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        imagePlace = findViewById(R.id.imagePlace);
        txtName = findViewById(R.id.txtName);
        txtAddress = findViewById(R.id.txtAddress);
        txtPhone = findViewById(R.id.txtPhone);
        txtRating = findViewById(R.id.txtRating);
        txtWebsite = findViewById(R.id.txtWebsite);
        txtOpenNow = findViewById(R.id.txtOpenNow);
        btnFavourite = findViewById(R.id.btnFavourite);

        placeId = getIntent().getStringExtra("place_id");

        loadPlaceDetails();
        setupClickableItems();

        // ⭐ Save to Favourite
        btnFavourite.setOnClickListener(v -> saveToFavourite());
    }

    private void setupClickableItems() {

        // 📞 CALL
        txtPhone.setOnClickListener(v -> {
            String phone = txtPhone.getText().toString();
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        // 🌐 WEBSITE
        txtWebsite.setOnClickListener(v -> {
            String url = txtWebsite.getText().toString();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // 🗺️ NAVIGATION
        txtAddress.setOnClickListener(v -> {
            if (lat != 0 && lng != 0) {
                String nav = "google.navigation:q=" + lat + "," + lng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(nav));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
    }

    private void saveToFavourite() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            SharedPreferences prefs = getSharedPreferences("favourites_" + userId, MODE_PRIVATE);

            String existing = prefs.getString("fav_list", "[]");

            JSONArray arr = new JSONArray(existing);

            // prevent duplicates
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (placeId.equals(obj.optString("placeId"))) {
                    Toast.makeText(this, "Already in favourites ⭐", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            JSONObject fav = new JSONObject();
            fav.put("placeId", placeId);
            fav.put("name", txtName.getText().toString());
            fav.put("address", txtAddress.getText().toString());
            fav.put("rating", txtRating.getText().toString());
            fav.put("lat", lat);
            fav.put("lng", lng);
            fav.put("photoRef", photoRef);

            arr.put(fav);
            prefs.edit().putString("fav_list", arr.toString()).apply();

            Toast.makeText(this, "Saved to favourites ⭐", Toast.LENGTH_SHORT).show();

            NotificationHelper.send(
                    this,
                    "⭐ Saved to Favourite",
                    txtName.getText().toString() + " added to favourites",
                    "favourite"
            );

        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPlaceDetails() {
        String url = "https://maps.googleapis.com/maps/api/place/details/json?" +
                "place_id=" + placeId +
                "&fields=name,rating,formatted_phone_number,formatted_address,opening_hours,website,geometry,photos" +
                "&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");

                        txtName.setText(result.optString("name"));
                        txtAddress.setText(result.optString("formatted_address"));
                        txtPhone.setText(result.optString("formatted_phone_number"));
                        txtRating.setText("Rating: " + result.optDouble("rating"));
                        txtWebsite.setText(result.optString("website"));

                        if (result.has("opening_hours")) {
                            boolean openNow = result
                                    .getJSONObject("opening_hours")
                                    .optBoolean("open_now");
                            txtOpenNow.setText(openNow ? "Open Now" : "Closed");
                        }

                        if (result.has("geometry")) {
                            JSONObject location = result
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");
                            lat = location.getDouble("lat");
                            lng = location.getDouble("lng");
                        }

                        // ✅ PHOTO
                        if (result.has("photos")) {
                            photoRef = result
                                    .getJSONArray("photos")
                                    .getJSONObject(0)
                                    .getString("photo_reference");

                            String photoUrl =
                                    "https://maps.googleapis.com/maps/api/place/photo" +
                                            "?maxwidth=800" +
                                            "&photo_reference=" + photoRef +
                                            "&key=" + API_KEY;

                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_cafe_placeholder)
                                    .into(imagePlace);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("PLACE_DETAILS", error.toString());
                }
        );

        queue.add(request);
    }
}
