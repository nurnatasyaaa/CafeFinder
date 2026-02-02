package com.example.cafefinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import android.media.AudioAttributes;
import android.media.RingtoneManager;

public class NotificationHelper {

    public static final String CHANNEL_ID = "cafe_alerts";

    public static void send(Context context, String title, String message, String type) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔹 Save to Firebase
        NotificationModel model =
                new NotificationModel(title, message, type, System.currentTimeMillis());

        FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(userId)
                .push()
                .setValue(model);

        // 🔹 Show system notification
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cafe Alerts",
                    NotificationManager.IMPORTANCE_HIGH   // 🔥 HIGH importance
            );

            channel.enableVibration(true);
            channel.enableLights(true);

            // 🔔 DEFAULT SYSTEM SOUND
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    audioAttributes
            );

            manager.createNotificationChannel(channel);
        }



        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // 🔊 SOUND + VIBRATION
                        .setAutoCancel(true);


        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
