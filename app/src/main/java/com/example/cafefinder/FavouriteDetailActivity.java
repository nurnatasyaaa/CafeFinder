package com.example.cafefinder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FavouriteDetailActivity extends AppCompatActivity {

    // UI
    TextView txtCafeName;
    EditText etNotes;
    Button btnSaveNotes, btnAddPhoto;
    RecyclerView recyclerImages, recyclerNotes;

    // Image gallery (current session)
    ArrayList<Bitmap> imageList = new ArrayList<>();
    FavouriteImageAdapter imageAdapter;

    // History logs
    ArrayList<LogModel> logList = new ArrayList<>();
    LogAdapter logAdapter;

    // Firebase
    DatabaseReference favRef;
    String placeId;

    // 📷 Camera launcher
    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) return;

                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        if (photo == null) return;

                        imageList.add(photo);
                        imageAdapter.notifyItemInserted(imageList.size() - 1);
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
        etNotes = findViewById(R.id.etNotes);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        recyclerImages = findViewById(R.id.recyclerImages);
        recyclerNotes = findViewById(R.id.recyclerNotes);

        // =====================
        // INTENT DATA
        // =====================
        placeId = getIntent().getStringExtra("place_id");
        txtCafeName.setText(getIntent().getStringExtra("name"));

        // =====================
        // FIREBASE (USER SCOPED)
        // =====================
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favRef = FirebaseDatabase.getInstance()
                .getReference("favourites")
                .child(userId)
                .child(placeId);

        // =====================
        // IMAGE PREVIEW (CURRENT)
        // =====================
        recyclerImages.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new FavouriteImageAdapter(this, imageList, pos -> {
            imageList.remove(pos);
            imageAdapter.notifyItemRemoved(pos);
        });
        recyclerImages.setAdapter(imageAdapter);

        // =====================
        // LOG HISTORY
        // =====================
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(logList);
        recyclerNotes.setAdapter(logAdapter);

        loadLogs();

        // =====================
        // CAMERA PERMISSION
        // =====================
        if (checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
        }

        // =====================
        // ADD PHOTO
        // =====================
        btnAddPhoto.setOnClickListener(v ->
                cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        );

        // =====================
        // SAVE / UPDATE
        // =====================
        btnSaveNotes.setOnClickListener(v -> saveLog());
    }

    // ==================================================
    // SAVE LOG (NOTE + ACTUAL IMAGES, NO OVERWRITE)
    // ==================================================
    private void saveLog() {
        String note = etNotes.getText().toString().trim();

        if (note.isEmpty() && imageList.isEmpty()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Create new log (push = history)
        DatabaseReference logRef = favRef.child("logs").push();

        logRef.child("note").setValue(note);
        logRef.child("timestamp").setValue(System.currentTimeMillis());

        // ✅ SAVE IMAGES AS BASE64 LIST
        ArrayList<String> encodedImages = new ArrayList<>();

        for (Bitmap bmp : imageList) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 90, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            encodedImages.add(base64);
        }

        logRef.child("images").setValue(encodedImages);

        // RESET UI
        etNotes.setText("");
        imageList.clear();
        imageAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show();
    }

    // =====================
    // LOAD HISTORY LOGS
    // =====================
    private void loadLogs() {
        favRef.child("logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logList.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    LogModel log = s.getValue(LogModel.class);
                    if (log != null) {
                        logList.add(0, log); // newest first
                    }
                }
                logAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
