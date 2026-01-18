package com.example.cafefinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavouriteImageAdapter
        extends RecyclerView.Adapter<FavouriteImageAdapter.ImageViewHolder> {

    Context context;
    List<Bitmap> imageList;
    OnImageLongClick listener;

    public interface OnImageLongClick {
        void onDelete(int position);
    }

    public FavouriteImageAdapter(Context context,
                                 List<Bitmap> imageList,
                                 OnImageLongClick listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_favourite_image, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.img.setImageBitmap(imageList.get(position));

        holder.img.setOnLongClickListener(v -> {
            listener.onDelete(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ImageViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.imgItem);
        }
    }
}
