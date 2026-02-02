package com.example.cafefinder;

public class NotificationModel {

    public String id;
    public String title;
    public String message;
    public String type;
    public long timestamp;

    public NotificationModel() {
        // Required for Firebase
    }

    public NotificationModel(String title, String message, String type, long timestamp) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
}
