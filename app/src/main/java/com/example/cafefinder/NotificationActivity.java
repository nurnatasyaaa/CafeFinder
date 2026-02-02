package com.example.cafefinder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<NotificationModel> list = new ArrayList<>();
    NotificationAdapter adapter;
    TextView btnDeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // ✅ FIND VIEWS AFTER setContentView
        recycler = findViewById(R.id.recyclerNotifications);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(list, this::confirmDeleteSingle);
        recycler.setAdapter(adapter);

        btnDeleteAll.setOnClickListener(v -> confirmDeleteAll());

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(userId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    NotificationModel n = s.getValue(NotificationModel.class);
                    if (n != null) {
                        n.id = s.getKey(); // ⭐ store Firebase key
                        list.add(0, n);   // newest first
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // =====================
    // DELETE SINGLE
    // =====================
    private void confirmDeleteSingle(NotificationModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Delete notification")
                .setMessage("Delete this notification?")
                .setPositiveButton("Delete", (d, w) -> deleteSingle(model))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSingle(NotificationModel model) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(userId)
                .child(model.id)
                .removeValue();
    }

    // =====================
    // DELETE ALL
    // =====================
    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all notifications")
                .setMessage("Delete all notifications?")
                .setPositiveButton("Delete", (d, w) -> deleteAll())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAll() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(userId)
                .removeValue();
    }
}
