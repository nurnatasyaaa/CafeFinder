package com.example.cafefinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogHolder> {

    ArrayList<LogModel> logs;

    public LogAdapter(ArrayList<LogModel> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder h, int position) {
        LogModel log = logs.get(position);

        h.txtNote.setText(log.note);

        String time = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date(log.timestamp));

        h.txtTime.setText(time);

        // ✅ Setup image preview recycler
        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        if (log.images != null) {
            for (String base64 : log.images) {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmaps.add(bmp);
            }
        }

        ImagePreviewAdapter imgAdapter = new ImagePreviewAdapter(bitmaps);
        h.recyclerImages.setLayoutManager(
                new LinearLayoutManager(h.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false)
        );
        h.recyclerImages.setAdapter(imgAdapter);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogHolder extends RecyclerView.ViewHolder {

        TextView txtNote, txtTime;
        RecyclerView recyclerImages;

        LogHolder(View v) {
            super(v);
            txtNote = v.findViewById(R.id.txtLogNote);
            txtTime = v.findViewById(R.id.txtLogTime);
            recyclerImages = v.findViewById(R.id.recyclerLogImages);
        }
    }
}
