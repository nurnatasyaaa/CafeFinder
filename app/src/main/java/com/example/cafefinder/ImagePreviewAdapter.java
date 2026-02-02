package com.example.cafefinder;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImagePreviewAdapter
        extends RecyclerView.Adapter<ImagePreviewAdapter.Holder> {

    ArrayList<Bitmap> images;

    public ImagePreviewAdapter(ArrayList<Bitmap> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.img.setImageBitmap(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView img;

        Holder(View v) {
            super(v);
            img = v.findViewById(R.id.imgLogPhoto);
        }
    }
}
