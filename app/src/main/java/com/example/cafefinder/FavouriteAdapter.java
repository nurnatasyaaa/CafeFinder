package com.example.cafefinder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FavouriteAdapter
        extends RecyclerView.Adapter<FavouriteAdapter.Holder> {

    Context context;
    ArrayList<String> favNames;

    public FavouriteAdapter(Context context, ArrayList<String> favNames) {
        this.context = context;
        this.favNames = favNames;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_favourite, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.txtName.setText(favNames.get(position));

        // CLICK → DETAILS
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FavouriteDetailActivity.class);
            intent.putExtra("name", favNames.get(position));
            intent.putExtra("place_id",
                    ((FavouriteActivity) context).favPlaceIds.get(position));
            context.startActivity(intent);
        });

        // LONG PRESS → DELETE
        h.itemView.setOnLongClickListener(v -> {
            ((FavouriteActivity) context).confirmDelete(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return favNames.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtName;

        Holder(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtCafeName);
        }
    }
}

