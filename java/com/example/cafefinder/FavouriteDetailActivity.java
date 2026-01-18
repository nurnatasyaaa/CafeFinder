package com.example.cafefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FavouriteDetailActivity extends AppCompatActivity {

    // UI
    TextView txtCafeName, txtCafeAddress, txtCafeRating;
    TextView tvSavedNote;
    EditText etNotes;
    Button btnSaveNotes, btnAddPhoto;
    RecyclerView recyclerImages;

    // Gallery
    FavouriteImageAdapter imageAdapter;
    ArrayList<Bitmap> imageList = new ArrayList<>();

    // Firebase
    DatabaseReference favRef;

    // Data
    String placeId;

    // 📷 Camera launcher
    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK) return;

                        if (result.getData() == null || result.getData().getExtras() == null) {
                            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bitmap photo = (Bitmap) result.getData()
                                .getExtras()
                                .get("data");

                        if (photo == null) {
                            Toast.makeText(this, "No image returned", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        imageList.add(photo);
                        imageAdapter.notifyItemInserted(imageList.size() - 1);

                        saveImagesToPrefs(); // your existing method
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_detail);

        // =====================
        // BIND UI
        // =====================
        txtCafeName = findViewById(R.id.txtCafeName);
        txtCafeAddress = findViewById(R.id.txtCafeAddress);
        txtCafeRating = findViewById(R.id.txtCafeRating);
        etNotes = findViewById(R.id.etNotes);
        tvSavedNote = findViewById(R.id.tvSavedNote);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        recyclerImages = findViewById(R.id.recyclerImages);

        // =====================
        // GET DATA FROM INTENT
        // =====================
        placeId = getIntent().getStringExtra("place_id");
        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        String rating = getIntent().getStringExtra("rating");

        txtCafeName.setText(name);
        txtCafeAddress.setText(address);
        txtCafeRating.setText(rating);

        // =====================
        // 🔐 USER-SCOPED FIREBASE (FIXED)
        // =====================
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        favRef = FirebaseDatabase.getInstance()
                .getReference("favourites")
                .child(userId)
                .child(placeId);

        // =====================
        // IMAGE GALLERY SETUP
        // =====================
        recyclerImages.setLayoutManager(new GridLayoutManager(this, 3));

        imageAdapter = new FavouriteImageAdapter(
                this,
                imageList,
                position -> deleteImage(position)
        );

        recyclerImages.setAdapter(imageAdapter);

        // =====================
        // LOAD EXISTING DATA
        // =====================
        loadImagesFromPrefs();
        loadNotesFromFirebase();

        // =====================
        // SAVE NOTES (LOCAL + ONLINE)
        // =====================
        btnSaveNotes.setOnClickListener(v -> {
            String note = etNotes.getText().toString().trim();

            if (note.isEmpty()) {
                Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // ONLINE
            favRef.child("note").setValue(note);
            favRef.child("updatedAt").setValue(System.currentTimeMillis());

            // LOCAL
            SharedPreferences prefs = getSharedPreferences("fav_notes", MODE_PRIVATE);
            prefs.edit().putString(placeId, note).apply();

            etNotes.setText("");
            Toast.makeText(this, "Notes saved ✓", Toast.LENGTH_SHORT).show();
        });

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.CAMERA},
                    100
            );
        }

        // =====================
        // ADD PHOTO
        // =====================
        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

    }


    // =====================
    // SAVE IMAGES (LOCAL ONLY)
    // =====================
    private void saveImagesToPrefs() {
        try {
            JSONArray arr = new JSONArray();

            for (Bitmap bitmap : imageList) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                arr.put(base64);
            }

            SharedPreferences prefs = getSharedPreferences("fav_images", MODE_PRIVATE);
            prefs.edit().putString(placeId, arr.toString()).apply();

            // metadata online
            favRef.child("imageCount").setValue(imageList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================
    // LOAD IMAGES
    // =====================
    private void loadImagesFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences("fav_images", MODE_PRIVATE);
            String json = prefs.getString(placeId, "[]");

            JSONArray arr = new JSONArray(json);
            imageList.clear();

            for (int i = 0; i < arr.length(); i++) {
                byte[] bytes = Base64.decode(arr.getString(i), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageList.add(bitmap);
            }

            imageAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================
    // DELETE IMAGE
    // =====================
    private void deleteImage(int position) {
        imageList.remove(position);
        imageAdapter.notifyItemRemoved(position);
        saveImagesToPrefs();
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
    }

    // =====================
    // LOAD NOTES (ONLINE → DISPLAY)
    // =====================
    private void loadNotesFromFirebase() {
        favRef.child("note").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvSavedNote.setText(snapshot.getValue(String.class));
                } else {
                    tvSavedNote.setText("No notes yet");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FavouriteDetailActivity.this,
                        "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
