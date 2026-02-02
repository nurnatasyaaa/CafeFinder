package com.example.cafefinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    ArrayList<NotificationModel> list;
    OnDeleteClick listener;

    public interface OnDeleteClick {
        void onDelete(NotificationModel model);
    }

    public NotificationAdapter(ArrayList<NotificationModel> list,
                               OnDeleteClick listener) {
        this.list = list;
        this.listener = listener;
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        NotificationModel n = list.get(pos);
        h.txtTitle.setText(n.title);
        h.txtMessage.setText(n.message);

        String time = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(new Date(n.timestamp));

        h.itemView.setOnLongClickListener(v -> {
            listener.onDelete(n);
            return true;
        });

        h.txtTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMessage, txtTime;

        Holder(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtMessage = v.findViewById(R.id.txtMessage);
            txtTime = v.findViewById(R.id.txtTime);
        }
    }
}
