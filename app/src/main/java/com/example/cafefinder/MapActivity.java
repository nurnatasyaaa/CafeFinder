package com.example.cafefinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 44;

    private FusedLocationProviderClient locationClient;
    private LatLng currentLatLng;

    private AutoCompleteTextView etSearch;
    private Button btnFindCafes;

    private PlacesClient placesClient;
    private ArrayAdapter<String> searchAdapter;
    private List<AutocompletePrediction> predictionList = new ArrayList<>();

    private static final String API_KEY = "YOUR_API_KEY_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        etSearch = findViewById(R.id.etSearch);
        btnFindCafes = findViewById(R.id.btnFindCafes);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }
        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        searchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        etSearch.setAdapter(searchAdapter);
        etSearch.setThreshold(1);

        requestLocationPermission();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1 || currentLatLng == null) return;

                double delta = 0.01;

                FindAutocompletePredictionsRequest request =
                        FindAutocompletePredictionsRequest.builder()
                                .setQuery(s.toString())
                                .setCountries("MY")
                                .setLocationBias(
                                        RectangularBounds.newInstance(
                                                new LatLng(
                                                        currentLatLng.latitude - delta,
                                                        currentLatLng.longitude - delta
                                                ),
                                                new LatLng(
                                                        currentLatLng.latitude + delta,
                                                        currentLatLng.longitude + delta
                                                )
                                        )
                                )
                                .build();

                placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener(response -> {

                            predictionList.clear();
                            searchAdapter.clear();

                            for (AutocompletePrediction p : response.getAutocompletePredictions()) {
                                predictionList.add(p);
                                searchAdapter.add(p.getFullText(null).toString());
                            }

                            searchAdapter.notifyDataSetChanged();
                            etSearch.showDropDown();
                        });
            }
        });

        etSearch.setOnItemClickListener((parent, view, position, id) -> {

            AutocompletePrediction selected = predictionList.get(position);

            FetchPlaceRequest request = FetchPlaceRequest.builder(
                    selected.getPlaceId(),
                    Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME)
            ).build();

            placesClient.fetchPlace(request)
                    .addOnSuccessListener(response -> {
                        Place place = response.getPlace();
                        LatLng latLng = place.getLatLng();

                        if (latLng != null) {
                            mMap.clear();
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(place.getName()));

                            marker.setTag(selected.getPlaceId());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                        }
                    });
        });

        btnFindCafes.setOnClickListener(v -> searchNearbyCafes());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() == null) return false;

            Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
            intent.putExtra("place_id", marker.getTag().toString());
            startActivity(intent);
            return true;
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        });
    }

    private void searchNearbyCafes() {
        if (currentLatLng == null) return;

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + currentLatLng.latitude + "," + currentLatLng.longitude
                + "&radius=2000&type=cafe&key=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        mMap.clear();

                        for (int i = 0; i < Math.min(results.length(), 10); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");

                            LatLng latLng = new LatLng(
                                    loc.getDouble("lat"),
                                    loc.getDouble("lng")
                            );

                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(obj.getString("name"))
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            marker.setTag(obj.getString("place_id"));
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading cafés", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}
